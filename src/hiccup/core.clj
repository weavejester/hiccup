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

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. #^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def h escape-html)  ;; Alias for escape-html

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
          (not value) ""
          :else (format-attr attr value))))))

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

(defn- uneval?
  "True if x is an unevaluated form or symbol."
  [x]
  (or (symbol? x)
      (and (seq? x)
           (not= (first x) `quote))))

(defn- literal?
  "True if x is a literal value that can be rendered as-is."
  [x]
  (and (not (uneval? x))
       (or (not (vector? x))
           (every? literal? x))))

(declare compile-html)

(defn- compile-lit-tag+attrs
  "Compile an element when only the tag and attributes are literal."
  [tag attrs content]
  (let [[tag attrs _] (parse-element [tag attrs])]
    (if (or content (container-tags tag))
      `(str ~(str "<" tag (make-attrs attrs) ">")
            ~@(compile-html content)
            ~(str "</" tag ">"))
       (str "<" tag (make-attrs attrs) " />"))))

(defn compile-lit-tag
  "Compile an element when only the tag is literal."
  [tag [attrs & content :as element]]
  (let [[tag tag-attrs _] (parse-element [tag])]
    `(if (map? ~attrs)
       ~(if (or content (container-tags tag))
          `(str ~(str "<" tag) (make-attrs (merge ~tag-attrs ~attrs)) ">"
                ~@(compile-html content)
                ~(str "</" tag ">"))
          `(str ~(str "<" tag) (make-attrs (merge ~tag-attrs ~attrs)) "/>"))
       ~(if (or element (container-tags tag))
          `(str ~(str "<" tag ">") 
                ~@(compile-html element)
                ~(str "</" tag ">"))
           (str "<" tag " />")))))

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
    (and (literal? tag) (or (not (uneval? attrs)) (not-hint? attrs Map)))
      (compile-lit-tag+attrs tag {} (cons attrs content))
    ;; e.g. [:span x]
    (literal? tag)
      (compile-lit-tag tag (cons attrs content))
    :else
      `(render-tag
         [~(first element)
          ~@(for [x (rest element)]
              (if (vector? x)
                (compile-tag x)
                x))])))

(defn- collapse-strs
  "Collapse nested str expressions into one, where possible."
  [expr]
  (if (seq? expr)
    (cons
      (first expr)
      (mapcat
       #(if (and (seq? %) (= (first %) (first expr) `str))
          (rest (collapse-strs %))
          (list (collapse-strs %)))
        (rest expr)))
    expr))

(defn- compile-html
  "Pre-compile data structures into HTML where possible."
  [content]
  (for [c content]
    (cond
      (vector? c)  (compile-tag c)
      (literal? c) c
      (hint? c String) c
      :else `(render-html ~c))))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [& content]
  (collapse-strs `(str ~@(compile-html content))))
