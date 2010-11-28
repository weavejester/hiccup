(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:use [clojure.contrib.def :only (defvar defvar-)]
        [clojure.contrib.java-utils :only (as-str)])
  (:import [clojure.lang IPersistentVector
                         ISeq]))

(defvar *html-mode* :xml
  "Determines the way tags and attributes are formatted. Defaults to :xml.")

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. ^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def h escape-html)  ; alias for escape-html

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

(defvar- re-tag
  #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"
  "Regular expression that parses a CSS-style id and class from a tag name.")

(defvar- container-tags
  #{"a" "b" "body" "dd" "div" "dl" "dt" "em" "fieldset" "form" "h1" "h2" "h3"
    "h4" "h5" "h6" "head" "html" "i" "label" "li" "ol" "pre" "script" "span"
    "strong" "style" "textarea" "ul" "option"}
  "A list of tags that need an explicit ending tag when rendered.")

(defn- normalize-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (IllegalArgumentException. (str tag " is not a valid tag name."))))
  (let [[_ tag id class] (re-matches re-tag (as-str tag))
        tag-attrs        {:id id
                          :class (if class (.replace ^String class "." " "))}
        map-attrs        (first content)]
    (if (map? map-attrs)
      [tag (merge tag-attrs map-attrs) (next content)]
      [tag tag-attrs content])))

(defmulti render-html
  "Turn a Clojure data type into a string of HTML."
  {:private true}
  type)

(defn- render-element
  "Render an tag vector as a HTML element."
  [element]
  (let [[tag attrs content] (normalize-element element)]
    (if (or content (container-tags tag))
      (str "<" tag (render-attr-map attrs) ">"
           (render-html content)
           "</" tag ">")
      (str "<" tag (render-attr-map attrs) (end-tag)))))

(defmethod render-html IPersistentVector
  [element]
  (render-element element))

(defmethod render-html ISeq [coll]
  (apply str (map render-html coll)))

(defmethod render-html :default [x]
  (as-str x))

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
    `(#'render-attr-map ~attrs)
    (render-attr-map attrs)))

(defn- form-name
  "Get the name of the supplied form."
  [form]
  (if (and (seq? form) (symbol? (first form)))
    (name (first form))))

(defmulti compile-form
  "Pre-compile certain standard forms, where possible."
  {:private true}
  form-name)

(defmethod compile-form "for"
  [[_ bindings body]]
  `(apply str (for ~bindings (html ~body))))

(defmethod compile-form "if"
  [[_ condition & body]]
  `(if ~condition ~@(for [x body] `(html ~x))))

(defmethod compile-form :default
  [expr]
  `(#'render-html ~expr))

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

(declare compile-html)

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
  (let [[tag attrs _] (normalize-element [tag attrs])]
    (if (or content (container-tags tag))
      `(str ~(str "<" tag) ~(compile-attr-map attrs) ">"
            ~@(compile-html content)
            ~(str "</" tag ">"))
      `(str "<" ~tag ~(compile-attr-map attrs) ~(end-tag)))))

(defmethod compile-element ::literal-tag-and-no-attributes
  [[tag & content]]
  (compile-element (apply vector tag {} content)))

(defmethod compile-element ::literal-tag
  [[tag attrs & content]]
  (let [[tag tag-attrs _] (normalize-element [tag])
        attrs-sym         (gensym "attrs")]
    `(let [~attrs-sym ~attrs]
       (if (map? ~attrs-sym)
         ~(if (or content (container-tags tag))
            `(str ~(str "<" tag)
                  (#'render-attr-map (merge ~tag-attrs ~attrs-sym)) ">"
                  ~@(compile-html content)
                  ~(str "</" tag ">"))
            `(str ~(str "<" tag)
                  (#'render-attr-map (merge ~tag-attrs ~attrs-sym))
                  ~(end-tag)))
         ~(if (or attrs (container-tags tag))
            `(str ~(str "<" tag (render-attr-map tag-attrs) ">")
                  ~@(compile-html (cons attrs-sym content))
                  ~(str "</" tag ">"))
            (str "<" tag (render-attr-map tag-attrs) (end-tag)))))))

(defmethod compile-element :default
  [element]
  `(#'render-element
     [~(first element)
      ~@(for [x (rest element)]
          (if (vector? x)
            (compile-element x)
            x))]))

(defn- compile-html
  "Pre-compile data structures into HTML where possible."
  [content]
  (doall (for [expr content]
           (cond
            (vector? expr) (compile-element expr)
            (literal? expr) expr
            (hint? expr String) expr
            (hint? expr Number) expr
            (seq? expr) (compile-form expr)
            :else `(#'render-html ~expr)))))

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

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (letfn [(make-html [content]
           (collapse-strs `(str ~@(compile-html content))))]
    (if-let [mode (and (map? options) (:mode options))]
      (binding [*html-mode* mode]
        `(binding [*html-mode* ~mode]
           ~(make-html content)))
      (make-html (cons options content)))))

(defmacro defhtml
  "Define a function, but wrap its output in an implicit html macro."
  [name & fdecl]
  (let [[fhead fbody] (split-with #(not (or (list? %) (vector? %))) fdecl)
        wrap-html (fn [[args & body]] `(~args (html ~@body)))]
    `(defn ~name
       ~@fhead
       ~@(if (vector? (first fbody))
           (wrap-html fbody)
           (map wrap-html fbody)))))

(defn add-optional-attrs
  "Add an optional attribute argument to a function that returns a vector tag."
  [func]
  (fn [& args]
    (if (map? (first args))
      (let [[tag & body] (apply func (rest args))]
        (if (map? (first body))
          (apply vector tag (merge (first body) (first args)) (rest body))
          (apply vector tag (first args) body)))
      (apply func args))))

(defmacro defelem
  "Defines a function that will return a tag vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned tag value."
  [name & fdecl]
  `(do (defn ~name ~@fdecl)
       (alter-var-root (var ~name) add-optional-attrs)))
