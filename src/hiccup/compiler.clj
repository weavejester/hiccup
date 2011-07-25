(ns hiccup.compiler
  (:refer-clojure :exclude (compile)))

(defn quoted? [x]
  (and (list? x)
       (= (first x) 'quote)))

(defn literal? [x]
  (or (number? x)
      (string? x)
      (keyword? x)
      (quoted? x)
      (and (vector? x) (every? literal? x))
      (and (map? x) (every? literal? x))))

(defn form-head [form]
  (if (and (seq? form) (symbol? (first form)))
    (-> form first name symbol)))

(defmulti return-type form-head)

(defmethod return-type 'for [_] :seq)
(defmethod return-type 'map [_] :seq)
(defmethod return-type 'filter [_] :seq)
(defmethod return-type 'list [_] :seq)
(defmethod return-type 'str [_] :string)
(defmethod return-type :default [_] :unknown)

(defn not-map? [x]
  (or (number? x)
      (string? x)
      (keyword? x)
      (vector? x)
      (and (seq? x)
           (not= (return-type x) :map)
           (not= (return-type x) :unknown))))

(defn assoc-unless-nil [m k v]
  (if (nil? v) m (assoc m k v)))

(defn content-seq [& coll]
  (mapcat #(if (seq? %) % (list %)) coll))

(declare render)

(let [tag-regex #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"]
  (defn parse-tag-sugar [tag]
    (rest (re-matches tag-regex (name tag)))))

(defn render-element [tag attrs content]
  (let [[tag id class] (parse-tag-sugar tag)]
    {:tag tag
     :attrs (-> attrs
                (assoc-unless-nil :id id)
                (assoc-unless-nil :class class))
     :content content}))

(defn render-vector [[tag & [attrs & content :as tail]]]
  (if (map? attrs)
    (render-element tag attrs (render content))
    (render-element tag {} (render tail))))

(defn render [node]
  (cond
   (vector? node) (render-vector node)
   (seq? node)    (apply content-seq (map render node))
   :else          node))

(declare compile)

(defn assoc-attr-sugar [attrs id class]
  (if (map? attrs)
    (-> attrs
        (assoc-unless-nil :id id)
        (assoc-unless-nil :class class))
    `(-> ~attrs
         ~@(if id [`(assoc :id ~id)])
         ~@(if class [`(assoc :class ~class)]))))

(defn compile-element [tag attrs content]
  (let [[tag id class] (parse-tag-sugar tag)]
    {:tag tag
     :attrs (assoc-attr-sugar attrs id class)
     :content content}))

(defn compile-attrs [attrs]
  (cond
   (literal? attrs) (if (map? attrs) attrs {})
   (not-map? attrs) {}
   :else
   `(let [attrs# ~attrs]
      (if (map? attrs#) attrs# {}))))

(defn compile-content [[attrs & content :as tail]]
  (cond
   (map? attrs)     `(content-seq ~@(map compile content))
   (not-map? attrs) `(content-seq ~@(map compile tail))
   :else
   `(let [content# (list ~@tail)]
      (if (map? (first content#))
        (render (rest content#))
        (render content#)))))

(defn compile-vector [[tag & [attrs & content :as tail]]]
  (compile-element
   tag
   (compile-attrs attrs)
   (compile-content tail)))

(defmulti compile-form form-head)

(defmethod compile-form 'for
  [[_ bindings body]]
  `(for ~bindings ~(compile body)))

(defmethod compile-form 'if
  [[_ condition & body]]
  `(if ~condition
     ~@(for [result body] (compile result))))

(defmethod compile-form :default [node]
  `(render node))

(defn compile [node]
  (cond
   (vector? node)  (compile-vector node)
   (literal? node) node
   (seq? node)     (compile-form node)
   :else           `(render ~node)))
