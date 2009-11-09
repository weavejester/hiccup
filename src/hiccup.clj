;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns hiccup
  "Efficiently generate HTML from a Clojure data structure."
  (:use clojure.contrib.def)
  (:use clojure.contrib.java-utils))

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (as-str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(defn- format-attr
  "Turn a key-value pair into a pair of attribute-value strings."
  [[key value]]
  (cond
    (true? value) [(as-str key) (escape-html key)]
    (not value)   nil
    :otherwise    [(as-str key) (escape-html value)]))

(defn- make-attrs
  "Turn a map into a string of HTML attributes, sorted by attribute name."
  [attrs]
  (remove nil?
    (for [[attr value] (sort (map format-attr attrs))]
      (if attr
        (str " " attr "=\"" value "\"")))))

(defvar- re-tag
  #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"
  "Regular expression that parses a CSS-style id and class from a tag name.")

(defn parse-tag-name
  "Parse the id and classes from a tag name."
  [tag]
  (rest (re-matches re-tag (as-str tag))))

(derive clojure.lang.IPersistentVector ::vector)
(derive clojure.lang.ISeq ::seq)

(defmulti render-html
  "Render a Clojure data structure to a seq of HTML at runtime."
  type)

(defn render-tag
  "Render a HTML tag."
  [tag-name content]
  (let [[tag id class] (parse-tag-name tag-name)
        attributes     (make-attrs {:id id, :class class})]
    (concat
      (list* "<" tag attributes)
      content
      (list "</" tag ">"))))

(defn render-attrs
  "Render an attribute map."
  [attrs content]
  (concat
    (if (map? attrs)
      (make-attrs attrs))
    content))

(defn render-content
  "Render the contents of a tag."
  [content]
  (cons
    ">"
    (render-html
      (if (map? (first content))
        (rest content)
        content))))

(defmethod render-html ::vector
  [[tag & content]]
  (render-tag tag
    (render-attrs (first content)
      (render-content content))))

(defmethod render-html ::seq
  [coll]
  (mapcat render-html coll))

(defmethod render-html :default
  [x]
  (list (as-str x)))

(defn quoted?
  "True if the form is a quoted value."
  [x]
  (and (list? x)
       (= 'quote (first x))))

(defn literal?
  "True if the value is a literal value."
  [x]
  (or (and (not (seq? x))
           (not (symbol? x)))
      (quoted? x)))

(derive clojure.lang.IPersistentList ::form)
(derive clojure.lang.Symbol ::form)

(defmulti compile-html
  "Render a Clojure data structure to a seq of HTML at compile-time when
  possible, at runtime when not."
  type)

(defn- collapse-strs
  "Concatenate adjacent strings in a sequence."
  [coll]
  (reduce
    (fn [xs y]
      (if (and (string? (first xs)) (string? y))
        (cons (str y (first xs)) (rest xs))
        (cons y xs)))
    '()
    (reverse coll)))

(defn compile-seq
  "Compile a sequence of Clojure data structures into HTML"
  [coll]
  (let [forms (collapse-strs (mapcat compile-html coll))]
    (if (every? literal? forms)
      `(list ~@forms)
      `(concat ~@(for [form forms]
                   (if (literal? form)
                     `(list ~form)
                     form))))))

(defn compile-tag
  "Attempt to pre-render a HTML tag."
  [tag-name content]
  (if (literal? tag-name)
    (render-tag tag-name content)
   `(render-tag ~tag-name ~(compile-seq content))))

(defn compile-attrs
  "Attempt to pre-render an attribute map."
  [attrs content]
  (if (map? attrs)
    (render-attrs attrs content)
    (if-not (literal? attrs)
     `((render-attrs ~attrs ~(compile-seq content)))
      content)))

(defn compile-content
  "Attempt to pre-render the contents of a tag."
  [content]
  (cons
    ">"
    (cond
      (map? (first content))
        (mapcat compile-html (rest content))
      (literal? (first content))
        (mapcat compile-html content)
      :else
        (concat
          (take 2 content)
          (mapcat compile-html (drop 2 content))))))

(defmethod compile-html ::vector
  [[tag & content]]
  (compile-tag tag
    (compile-attrs (first content)
      (compile-content content))))

(defmethod compile-html ::form
  [form]
  `((render-html ~form)))

(defmethod compile-html :default
  [x]
  (render-html x))

(defmacro html
  "Efficiently render a Clojure data structure into a seq of HTML."
  [& content]
  (compile-seq content))
