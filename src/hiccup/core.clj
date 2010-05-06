;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns hiccup.core
  "Renders a tree of vectors into a string of HTML. Pre-compiles where
  possible."
  (:use clojure.contrib.def
        clojure.contrib.java-utils)
  (:import java.util.Map))

(def *html-mode* :xml)

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. #^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def h escape-html)  ;; Alias for escape-html

(defn- tag-end
  "Return the ending part of a self-closing tag."
  []
  (if (= *html-mode* :xml)
    " />"
    ">"))

(defn- format-attr
  "Turn a name/value pair into an attribute stringp"
  [name value]
  (str " " (as-str name) "=\"" (escape-html value) "\""))

(defn- render-attrs
  "Turn a map into a string of sorted HTML attributes."
  [attrs]
  (apply str
    (sort
      (for [[attr value] attrs]
        (cond
          (true? value)
            (if (= *html-mode* :xml)
              (format-attr attr attr)
              (str " " (as-str attr)))
          (not value)
            ""
          :else
            (format-attr attr value))))))

(defn- uneval?
  "True if x is an unevaluated form or symbol."
  [x]
  (or (symbol? x)
      (and (seq? x)
           (not= (first x) `quote))))

(defn- compile-attrs
  "Turn a map with unevaluated symbols into an expression that will render the
  corresponding attributes."
  [attrs]
  (if (some uneval? (mapcat identity attrs))
    `(#'render-attrs ~attrs)
    (render-attrs attrs)))

(defvar- re-tag
  #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"
  "Regular expression that parses a CSS-style id and class from a tag name.")

(defvar- container-tags
  #{"a" "b" "body" "dd" "div" "dl" "dt" "em" "fieldset" "form" "h1" "h2" "h3"
    "h4" "h5" "h6" "head" "html" "i" "label" "li" "ol" "pre" "script" "span"
    "strong" "style" "textarea" "ul"}
  "A list of tags that need an explicit ending tag when rendered.")

(defn- parse-tag-name
  "Parse the id and classes from a tag name."
  [tag]
  (rest (re-matches re-tag (as-str tag))))

(defn- parse-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (let [[tag id class] (parse-tag-name tag)
        tag-attrs      {:id id
                        :class (if class (.replace #^String class "." " "))}
        map-attrs      (first content)]
    (if (map? map-attrs)
      [tag (merge tag-attrs map-attrs) (next content)]
      [tag tag-attrs content])))

(declare render-html)

(defn- render-tag
  "Render a HTML tag represented as a vector."
  [element]
  (let [[tag attrs content] (parse-element element)]
    (if (or content (container-tags tag))
      (str "<" tag (render-attrs attrs) ">"
           (render-html content)
           "</" tag ">")
      (str "<" tag (render-attrs attrs) (tag-end)))))

(defn- render-html
  "Render a Clojure data structure to a string of HTML."
  [data]
  (cond
    (vector? data) (render-tag data)
    (seq? data) (apply str (map render-html data))
    :else (as-str data)))

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
  (and (not (uneval? x))
       (or (not (or (vector? x) (map? x)))
           (every? literal? x))))

(defn- form-name
  "Get the name of the supplied form."
  [form]
  (if (and (seq? form) (symbol? (first form)))
    (name (first form))))

(defn- not-implicit-map?
  "True if we can infer that x is not a map."
  [x]
  (or (= (form-name x) "for")
      (not (uneval? x))
      (not-hint? x Map)))

(declare compile-html)

(defn- compile-lit-tag+attrs
  "Compile an element when only the tag and attributes are literal."
  [tag attrs content]
  (let [[tag attrs _] (parse-element [tag attrs])]
    (if (or content (container-tags tag))
      `(str ~(str "<" tag) ~(compile-attrs attrs) ">"
            ~@(compile-html content)
            ~(str "</" tag ">"))
      `(str "<" ~tag ~(compile-attrs attrs) ~(tag-end)))))

(defn- compile-lit-tag
  "Compile an element when only the tag is literal."
  [tag [attrs & content :as element]]
  (let [[tag tag-attrs _] (parse-element [tag])]
    `(if (map? ~attrs)
       ~(if (or content (container-tags tag))
          `(str ~(str "<" tag) (#'render-attrs (merge ~tag-attrs ~attrs)) ">"
                ~@(compile-html content)
                ~(str "</" tag ">"))
          `(str ~(str "<" tag) (#'render-attrs (merge ~tag-attrs ~attrs))
                ~(tag-end)))
       ~(if (or element (container-tags tag))
          `(str ~(str "<" tag (render-attrs tag-attrs) ">")
                ~@(compile-html element)
                ~(str "</" tag ">"))
          (str "<" tag (render-attrs tag-attrs) (tag-end))))))

(defn- compile-tag 
  "Pre-compile a single tag vector where possible."
  [[tag attrs & content :as element]]
  (cond
    ;; e.g. [:span "foo"]
    (every? literal? element)
      (render-tag (eval element))
    ;; e.g. [:span {} x]
    (and (literal? tag) (map? attrs))
      (compile-lit-tag+attrs tag attrs content)
    ;; e.g. [:span #^String x]
    (and (literal? tag) (not-implicit-map? attrs))
      (compile-lit-tag+attrs tag {} (cons attrs content))
    ;; e.g. [:span x]
    (literal? tag)
      (compile-lit-tag tag (cons attrs content))
    :else
      `(#'render-tag
         [~(first element)
          ~@(for [x (rest element)]
              (if (vector? x)
                (compile-tag x)
                x))])))

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

(defn- compile-html
  "Pre-compile data structures into HTML where possible."
  [content]
  (for [expr content]
    (cond
      (vector? expr) (compile-tag expr)
      (literal? expr) expr
      (hint? expr String) expr
      (hint? expr Number) expr
      (seq? expr) (compile-form expr)
      :else `(#'render-html ~expr))))

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

(defmacro defelem
  "Defines a function adding an optional map argument in the first position.
   fdecl has the same form used by clojure.core/defn. The resuting function must return a vector.
   If the first argument passed to the result function is a map, it will be inserted as second element in vector.
   If in that position there is a map already, both will be merged"
  [name & fdecl]
  (let [[m fdecl] (if (string? (first fdecl))
                    [{:doc (first fdecl)} (next fdecl)]
                    [{} fdecl])
        [m fdecl] (if (map? (first fdecl))
                    [(conj m (first fdecl)) (next fdecl)]
                    [m fdecl])
        [m fdecl] (if (map? (last fdecl))
                    [(conj m (last fdecl)) (butlast fdecl)]
                    [m fdecl])
        m (conj (or (meta name) {}) m)]

    `(defn ~name ~m [& [atts# & more# :as args#]]
       (let [f# (fn ~name ~@fdecl)]
         (if (map? atts#)
           (let [[tag# & r#] (apply f# more#)]
             (vec (if (map? (first r#))
                    (list* tag# (merge (first r#) atts#) (next r#))
                    (list* tag# atts# r#))))
           (apply f# args#))))))

