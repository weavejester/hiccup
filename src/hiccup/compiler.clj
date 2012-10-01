(ns hiccup.compiler
  "Internal functions for compilation."
  (:use hiccup.util
        [clojure.walk :only [postwalk]])
  (:import [clojure.lang IPersistentVector IPersistentMap ISeq]))

(def ^:dynamic *html-mode* :xml)

(defn- xml-mode? []
  (= *html-mode* :xml))

(defn- end-tag []
  (if (xml-mode?) " />" ">"))

(defn- xml-attribute [name value]
  (str " " (as-str name) "=\"" (escape-html value) "\""))

(defn- render-attribute [[name value]]
  (cond
    (true? value)
      (if (xml-mode?)
        (xml-attribute name name)
        (str " " (as-str name)))
    (not value)
      ""
    :else
      (xml-attribute name value)))

(defn- render-attr-map [attrs]
  (apply str
    (sort (map render-attribute attrs))))

(def ^{:doc "Regular expression that parses a CSS-style id and class from an element name."
       :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def ^{:doc "A list of elements that need an explicit ending tag when rendered."
       :private true}
  container-tags
  #{"a" "article" "aside" "b" "body" "canvas" "dd" "div" "dl" "dt" "em" "fieldset"
    "footer" "form" "h1" "h2" "h3" "h4" "h5" "h6" "head" "header" "hgroup" "html"
    "i" "iframe" "label" "li" "nav" "ol" "option" "pre" "section" "script" "span"
    "strong" "style" "table" "textarea" "title" "ul"})

(defn- void?
  "True if x is semantically void."
  [x]
  (or (nil? x) (and (or (string? x) (coll? x)) (empty? x))))

(defn- unevaluated?
  "True if the expression has not been evaluated."
  [expr]
  (or (symbol? expr)
      (and (seq? expr)
           (not= (first expr) `quote))))

(defn- form-name
  "Get the name of the supplied form."
  [form]
  (if (and (seq? form) (symbol? (first form)))
    (name (first form))))

(defn- compile-attr-map
  "Returns an unevaluated attributes map with empty keys removed."
  [attrs]
  (let [attrs (apply concat (remove (comp void? val) attrs))]
    (if (some unevaluated? attrs)
      `(hash-map ~@attrs)
      (apply hash-map attrs))))

(defn normalize-element
  "Ensure an element vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (IllegalArgumentException. (str tag " is not a valid element name."))))
  (let [[_ tag id class] (re-matches re-tag (name tag))
        [attrs content] (if (map? (first content))
                          [(first content) (next content)]
                          [{} content])
        id (or (attrs :id) id)
        class (when (or class (attrs :class))
                (-> (str class " " (attrs :class)) (.replace "." " ") .trim))]
    [tag (assoc attrs :id id :class class) content]))

(defmulti build-form
  "Return a map representation of a Clojure data type."
  {:private true}
  type)

(defn- element-map
  "Convert an element vector to an element map."
  [element]
  (let [[tag attrs content] (normalize-element element)]
    {:tag tag :attrs (compile-attr-map attrs) :content (build-form content)}))

(defmethod build-form IPersistentMap
  [element]
  element)

(defmethod build-form IPersistentVector
  [element]
  (element-map element))

(defmethod build-form ISeq
  [coll]
  (apply vector (map build-form coll)))

(defmethod build-form :default [x]
  (when x (as-str x)))

(defmulti compile-form
  "Pre-compile certain standard forms where possible."
  {:private true}
  form-name)

(declare compile-forms)

(defmethod compile-form "for"
  [[_ bindings body]]
  `(apply vector (for ~bindings ~(compile-forms body))))

(defmethod compile-form "if"
  [[_ condition & body]]
  `(if ~condition ~@(for [x body] (compile-forms x))))

(defmethod compile-form :default
  [expr]
  `(#'build-form ~expr))

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
  "Returns a clojure map representation of the supplied vector."
  {:private true}
  element-compile-strategy)

(defmethod compile-element ::all-literal
  [element]
  (if (seq? (first element))
    (element-map (cons (eval (first element)) (rest element)))
    (element-map element)))

(defmethod compile-element ::literal-tag-and-attributes
  [[tag attrs & content]]
  (let [[tag attrs _] (normalize-element [tag attrs])]
    (if content
      `(hash-map :tag ~tag
                 :attrs ~(compile-attr-map attrs)
                 :content [~@(compile-seq content)])
      `(hash-map :tag ~tag
                 :attrs ~(compile-attr-map attrs)))))

(defmethod compile-element ::literal-tag-and-no-attributes
  [[tag & content]]
  (compile-element (apply vector tag {} content)))

(defmethod compile-element ::literal-tag
  [[tag attrs & content]]
  (let [[tag tag-attrs _] (normalize-element [tag])
        attrs-sym         (gensym "attrs")]
    `(let [~attrs-sym ~attrs]
       (if (map? ~attrs-sym)
         ~(if content
            `(hash-map :tag ~tag
                       :attrs (#'compile-attr-map (merge ~tag-attrs ~attrs-sym))
                       :content [~@(compile-seq content)])
            `(hash-map :tag ~tag
                       :attrs (#'compile-attr-map (merge ~tag-attrs ~attrs-sym))))
         ~(if attrs
            `(hash-map :tag ~tag
                       :attrs ~(compile-attr-map tag-attrs)
                       :content [~@(compile-seq (cons attrs-sym content))])
            (hash-map :tag tag :attrs {} :content [(compile-seq content)]))))))

(defmulti render-html
  "Turn a Clojure data type into a string of HTML."
  type)

(defn render-element
  "Returns an unevaluated form that will render the supplied map as an
   HTML element."
  [element]
  (let [{:keys [tag attrs content]} element]
    (if (and (nil? tag) (nil? attrs))
      (render-html content)
      (if (or content (container-tags tag))
        (str "<" tag (render-attr-map attrs) ">"
             (render-html content)
             "</" tag ">")
        (str "<" tag (render-attr-map attrs) (end-tag))))))

(defmethod render-html IPersistentVector
  [element]
  (apply str (map render-html element)))

(defmethod render-html IPersistentMap
  [element]
  (render-element element))

(defmethod render-html :default [x]
  (as-str x))

(defn- compile-seq
  "Compile a sequence of data structures into a Clojure map representation."
  [content]
  (doall (for [expr content]
           (cond
             (map? expr) (apply compile-forms (:content expr))
             (vector? expr) (compile-element expr)
             (literal? expr) expr
             (hint? expr String) expr
             (hint? expr Number) expr
             (seq? expr) (compile-form expr)
             :else `(#'build-form ~expr)))))

(defn collapse-content
  "Collapse nested content maps into one where possible."
  [expr]
  (->> expr
       (postwalk #(if (and (map? %) (= (keys %) [:content])) (:content %) %))
       (tree-seq vector? seq)
       rest
       (filter (complement vector?))
       vec))

(defn- pre-parse
  "Exchange intermediate calls to html for calls to parse."
  [expr]
  (postwalk #(if (= 'html %) 'parse %) expr))

(defn compile-forms
  "Pre-compile data structures into a Clojure map representation."
  [& content]
  {:content `(collapse-content [~@(compile-seq (pre-parse content))])})

(defn compile-html
  "Pre-compile data structures into HTML where possible."
  [& content]
  `(render-html ~(apply compile-forms content)))
