(ns hiccup.compiler
  "Internal functions for compilation."
  (:require [hiccup.util :as util]
            [clojure.string :as str])
  (:import [clojure.lang IPersistentVector ISeq Named]
           [hiccup.util RawString]))

(defn- xml-mode? []
  (#{:xml :xhtml} util/*html-mode*))

(defn- html-mode? []
  (#{:html :xhtml} util/*html-mode*))

(defn escape-html
  "Change special characters into HTML character entities if
  hiccup.util/*escape-strings* is true."
  [text]
  (if util/*escape-strings?*
    (util/escape-html text)
    text))

(defn- end-tag []
  (if (xml-mode?) " />" ">"))

(defn- render-style-map [value]
  (->> value
       (map (fn [[k v]] (str (util/as-str k) ":" v ";")))
       (sort)
       (apply str)))

(defn- render-attr-value [value]
  (cond
    (map? value)
      (render-style-map value)
    (sequential? value)
      (str/join " " (map util/as-str value))
    :else
      value))

(defn- xml-attribute [name value]
  (str " " (util/as-str name) "=\"" (util/escape-html (render-attr-value value)) "\""))

(defn- render-attribute [[name value]]
  (cond
    (true? value)
      (if (xml-mode?)
        (xml-attribute name name)
        (str " " (util/as-str name)))
    (not value)
      ""
    :else
      (xml-attribute name value)))

(defn render-attr-map
  "Render a map of attributes."
  [attrs]
  (apply str (sort (map render-attribute attrs))))

(def ^{:doc "Regular expression that parses a CSS-style id and class from an element name."
       :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def ^{:doc "A list of elements that must be rendered without a closing tag."
       :private true}
  void-tags
  #{"area" "base" "br" "col" "command" "embed" "hr" "img" "input" "keygen" "link"
    "meta" "param" "source" "track" "wbr"})

(defn- container-tag?
  "Returns true if the tag has content or is not a void tag. In non-HTML modes,
  all contentless tags are assumed to be void tags."
  [tag content]
  (or content
      (and (html-mode?) (not (void-tags tag)))))

(defn merge-classes [class classes]
  (cond
    (nil? class)    classes
    (string? class) (str classes " " class)
    :else           (str classes " " (str/join " " (map name class)))))

(declare literal?)

(defn- merge-classes-form [class-form classes]
  (if (literal? class-form)
    (merge-classes class-form classes)
    `(merge-classes ~class-form ~classes)))

(defn- merge-attributes [map-attrs id classes]
  (-> map-attrs
      (cond-> id      (assoc :id (or (:id map-attrs) id)))
      (cond-> classes (assoc :class (merge-classes (:class map-attrs) classes)))))

(defn- merge-attributes-form [map-attrs id classes]
  (-> map-attrs
      (cond-> id      (assoc :id (or (:id map-attrs) id)))
      (cond-> classes (assoc :class (merge-classes-form (:class map-attrs) classes)))))

(defn- normalize-element*
  [[tag & content] merge-attributes-fn]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (IllegalArgumentException. (str tag " is not a valid element name."))))
  (let [[_ tag id class] (re-matches re-tag (util/as-str tag))
        classes          (if class (str/replace class "." " "))
        map-attrs        (first content)]
    (if (map? map-attrs)
      [tag (merge-attributes-fn map-attrs id classes) (next content)]
      [tag {:id id, :class classes} content])))

(defn normalize-element
  "Ensure an element vector is of the form [tag-name attrs content]." 
  [[tag & content :as tag-content]]
  (normalize-element* tag-content merge-attributes))

(defn- normalize-element-form
  [[tag & content :as tag-content]]
  (normalize-element* tag-content merge-attributes-form))

(defprotocol HtmlRenderer
  (render-html [this]
    "Turn a Clojure data type into a string of HTML."))

(defn render-element
  "Render an element vector as a HTML element."
  [element]
  (let [[tag attrs content] (normalize-element element)]
    (if (container-tag? tag content)
      (str "<" tag (render-attr-map attrs) ">"
           (render-html content)
           "</" tag ">")
      (str "<" tag (render-attr-map attrs) (end-tag)))))

(extend-protocol HtmlRenderer
  IPersistentVector
  (render-html [this]
    (render-element this))
  ISeq
  (render-html [this]
    (apply str (map render-html this)))
  RawString
  (render-html [this]
    (str this))
  Named
  (render-html [this]
    (escape-html (name this)))
  Object
  (render-html [this]
    (escape-html (str this)))
  nil
  (render-html [this]
    ""))

(defn- unevaluated?
  "True if the expression has not been evaluated."
  [expr]
  (or (symbol? expr)
      (and (seq? expr)
           (not= (first expr) `quote))))

(defn compile-attr-map
  "Returns an unevaluated form that will render the supplied map as HTML
  attributes."
  [attrs]
  (if (some unevaluated? (mapcat identity attrs))
    `(render-attr-map ~attrs)
    (render-attr-map attrs)))

(defn- form-name
  "Get the name of the supplied form."
  [form]
  (if (and (seq? form) (symbol? (first form)))
    (name (first form))))

(declare compile-html)

(defmulti compile-form
  "Pre-compile certain standard forms, where possible."
  {:private true}
  form-name)

(defmethod compile-form "for"
  [[_ bindings body]]
  `(apply str (for ~bindings ~(compile-html body))))

(defmethod compile-form "if"
  [[_ condition & body]]
  `(if ~condition ~@(for [x body] (compile-html x))))

(defmethod compile-form :default
  [expr]
  `(render-html ~expr))

(defn- not-hint?
  "True if x is not hinted to be the supplied type."
  [x type]
  (if-let [hint (-> x meta :tag)]
    (not (isa? (eval hint) type))))

(defn- hint?
  "True if x is hinted to be the supplied type."
  [x type]
  (if-let [hint (-> x meta :tag)]
    (isa? (eval hint) type)))

(defn- literal?
  "True if x is a literal value that can be rendered as-is."
  [x]
  (and (not (unevaluated? x))
       (or (not (or (vector? x) (map? x)))
           (every? literal? x))))

(defn- not-implicit-map?
  "True if we can infer that x is not a map."
  [x]
  (or (= (form-name x) "for")
      (not (unevaluated? x))
      (not-hint? x java.util.Map)))

(defn- element-compile-strategy
  "Returns the compilation strategy to use for a given element."
  [[tag attrs & content :as element]]
  (cond
    (every? literal? element)
      ::all-literal                    ; e.g. [:span "foo"]
    (and (literal? tag) (map? attrs))
      ::literal-tag-and-attributes     ; e.g. [:span {} x]
    (and (literal? tag) (not-implicit-map? attrs))
      ::literal-tag-and-no-attributes  ; e.g. [:span ^String x]
    (literal? tag)
      ::literal-tag                    ; e.g. [:span x]
    :else
      ::default))                      ; e.g. [x]

(declare compile-seq)

(defmulti compile-element
  "Returns an unevaluated form that will render the supplied vector as a HTML
  element."
  {:private true}
  element-compile-strategy)

(defmethod compile-element ::all-literal
  [element]
  (render-element (eval element)))

(defmethod compile-element ::literal-tag-and-attributes
  [[tag attrs & content]]
  (let [[tag attrs _] (normalize-element-form [tag attrs])]
    (if (container-tag? tag content)
      `(str ~(str "<" tag) ~(compile-attr-map attrs) ">"
            ~@(compile-seq content)
            ~(str "</" tag ">"))
      `(str "<" ~tag ~(compile-attr-map attrs) ~(end-tag)))))

(defmethod compile-element ::literal-tag-and-no-attributes
  [[tag & content]]
  (compile-element (apply vector tag {} content)))

(defmethod compile-element ::literal-tag
  [[tag attrs & content]]
  (let [[tag tag-attrs _] (normalize-element-form [tag])
        attrs-sym         (gensym "attrs")]
    `(let [~attrs-sym ~attrs]
       (if (map? ~attrs-sym)
         ~(if (container-tag? tag content)
            `(str ~(str "<" tag)
                  (render-attr-map (merge ~tag-attrs ~attrs-sym)) ">"
                  ~@(compile-seq content)
                  ~(str "</" tag ">"))
            `(str ~(str "<" tag)
                  (render-attr-map (merge ~tag-attrs ~attrs-sym))
                  ~(end-tag)))
         ~(if (container-tag? tag attrs)
            `(str ~(str "<" tag (render-attr-map tag-attrs) ">")
                  ~@(compile-seq (cons attrs-sym content))
                  ~(str "</" tag ">"))
            (str "<" tag (render-attr-map tag-attrs) (end-tag)))))))

(defmethod compile-element :default
  [element]
  `(render-element
     [~(first element)
      ~@(for [x (rest element)]
          (if (vector? x)
            (compile-element x)
            x))]))

(defn- compile-seq
  "Compile a sequence of data-structures into HTML."
  [content]
  (doall (for [expr content]
           (cond
            (vector? expr) (compile-element expr)
            (string? expr) (escape-html expr)
            (keyword? expr) (escape-html (name expr))
            (util/raw-string? expr) expr
            (literal? expr) (escape-html expr)
            (hint? expr String) `(escape-html ~expr)
            (hint? expr Number) expr
            (seq? expr) (compile-form expr)
            :else `(render-html ~expr)))))

(defn- collapse-strs
  "Collapse nested str expressions into one, where possible."
  [expr]
  (if (seq? expr)
    (cons
     (first expr)
     (mapcat
      #(if (and (seq? %) (symbol? (first %)) (= (first %) (first expr) `str))
         (rest (collapse-strs %))
         (list (collapse-strs %)))
      (rest expr)))
    expr))

(defn compile-html
  "Pre-compile data structures into HTML where possible."
  [& content]
  (collapse-strs `(str ~@(compile-seq content))))

(defn- binding* [var val func]
  (push-thread-bindings {var val})
  (try (func)
       (finally (pop-thread-bindings))))

(defn- compile-multi [var-sym vals step]
  (let [var            (find-var var-sym)
        compiled-forms (->> vals
                            (map (fn [v] [v (binding* var v step)]))
                            (into {}))
        distinct-forms (->> compiled-forms
                            (group-by second)
                            (map (fn [[k v]] [(map first v) k])))]
    (cond
      (= (count distinct-forms) 1)
        (second (first distinct-forms))
      (= (set vals) #{true false})
        `(if ~var-sym ~(compiled-forms true) ~(compiled-forms false))
      :else
        `(case ~var-sym ~@(apply concat distinct-forms)))))

(defn compile-html-with-bindings
  "Pre-compile data structures into HTML where possible, while taking into
  account bindings that modify the result like *html-mode*."
  [& content]
  (let [step1 (fn [] (apply compile-html content))
        step2 (fn [] (compile-multi `util/*escape-strings?* [true false] step1))
        step3 (fn [] (compile-multi `util/*html-mode* [:html :xhtml :xml :sgml] step2))]
    (step3)))
