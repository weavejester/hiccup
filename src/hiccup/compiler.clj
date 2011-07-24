(ns hiccup.compiler
  (:refer-clojure :exclude (compile))
  (:require [clojure.walk :as walk]))

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

(defn not-map? [x]
  (or (number? x)
      (string? x)
      (keyword? x)
      (vector? x)))

(declare render)

(defn render-vector [[tag & [attrs & content :as tail]]]
  (if (map? attrs)
    {:tag tag, :attrs attrs, :content (render content)}
    {:tag tag, :attrs {}, :content (render tail)}))

(defn render [node]
  (cond
   (vector? node) (render-vector node)
   (seq? node)    (map render node)
   :else          node))

(declare compile)

(defn compile-attrs [attrs]
  (cond
   (literal? attrs) (if (map? attrs) attrs {})
   (not-map? attrs) {}
   :else
   `(let [attrs# ~attrs]
      (if (map? attrs#) attrs# {}))))

(defn compile-content [[attrs & content :as tail]]
  (cond
   (map? attrs)     `(list ~@(map compile content))
   (not-map? attrs) `(list ~@(map compile tail))
   :else
   `(let [content# (list ~@tail)]
      (if (map? (first content#))
        (render (rest content#))
        (render content#)))))

(defn compile-vector [[tag & [attrs & content :as tail]]]
  (if (map? attrs)
    {:tag tag, :attrs attrs, :content `(render (list ~@content))}
    {:tag tag
     :attrs (compile-attrs attrs)
     :content (compile-content tail)}))

(defn compile [node]
  (if (vector? node)
    (compile-vector node)
    node))
