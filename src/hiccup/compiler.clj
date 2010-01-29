;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns hiccup.compiler
  "Renders a tree of vectors into a string of HTML. Pre-compiles where
  possible."
  (:use clojure.contrib.def
        clojure.contrib.java-utils))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. #^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(defn- format-attr
  "Turn a name/value pair into an attribute stringp"
  [name value]
  (str " " (as-str name) "=\"" (escape-html value) "\""))

(defn make-attrs
  "Turn a map into a string of sorted HTML attributes."
  [attrs]
  (apply str
    (sort
      (for [[attr value] attrs]
        (cond
          (true? value) (format-attr attr attr)
          (not value)   ""
          :otherwise    (format-attr attr value))))))

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

(defn render-tag
  "Render a HTML tag represented as a vector."
  [element]
  (let [[tag attrs content] (parse-element element)]
    (if (or content (container-tags tag))
      (str "<" tag (make-attrs attrs) ">"
           (render-html content)
           "</" tag ">")
      (str "<" tag (make-attrs attrs) " />"))))

(defn render-html
  "Render a Clojure data structure to a string of HTML."
  [data]
  (cond
    (vector? data) (render-tag data)
    (seq? data)    (apply str (map render-html data))
    :otherwise     (as-str data)))

(defmacro match-pattern
  "Match and destructure the supplied pattern."
  [target pattern & body]
  (let [binds (map #(if (seq? %) (last %) %) pattern)
        preds (remove symbol? pattern)
        target-sym (gensym target)
        length-eq? (if-not (contains? (set pattern) '&)
                    `((= (count ~target-sym) ~(count pattern))))]
    `(let [~target-sym ~target, [~@binds] ~target-sym]
       (when (and ~@length-eq? ~@preds)
         ~@body))))

(defmacro case-pattern
  "A case statement that employs match-pattern."
  [target & clauses]
  `(or ~@(for [[pattern body] (partition 2 clauses)]
           `(match-pattern ~target ~pattern ~body))))

(defn- literal?
  "True if x is a literal value that can be rendered as-is."
  [x]
  (and (not (symbol? x))
       (or (not (vector? x))
           (every? literal? x))
       (or (not (seq? x))
           (= (first x) `quote))))

(declare compile-html)

(defn- compile-partial-tag
  "Compile an element when only the tag and attributes are literal."
  [tag attrs content]
  (let [[tag attrs _] (parse-element [tag attrs])]
    (if (or content (container-tags tag))
      `(str ~(str "<" tag (make-attrs attrs) ">")
            ~@(compile-html content)
            ~(str "</" tag ">"))
       (str "<" tag (make-attrs attrs) " />"))))

(defn compile-tag 
  "Pre-compile a single tag vector where possible."
  [element]
  (case-pattern element
    [& (every? literal? content)]
      (render-tag (eval element))
    [(literal? tag) (map? attrs) & content]
      (compile-partial-tag tag attrs content)
    [& content]
      `(render-tag [~@(compile-html content)])))

(defn compile-html
  "Pre-compile data structures into HTML where possible."
  [content]
  (for [c content]
    (cond
      (vector? c)  (compile-tag c)
      (literal? c) c
      :else        `(render-html ~c))))
