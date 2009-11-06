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

(derive clojure.lang.IPersistentVector ::vector)
(derive clojure.lang.IPersistentList ::list)
(derive clojure.lang.ISeq ::seq)

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (as-str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(defn quoted?
  "True if the form is a quoted value."
  [x]
  (and (list? x)
       (= 'quote (first x))))

(defn literal?
  "True if the value is a not a list or a quoted value."
  [x]
  (or (not (list? x))
      (quoted? x)))

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
  (list*
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

(defn collapse-strs
  "Concatenate adjacent strings in a sequence."
  [coll]
  (reduce
    (fn [xs y]
      (if (and (string? (first xs)) (string? y))
        (cons (str y (first xs)) (rest xs))
        (cons y xs)))
    '()
    (reverse coll)))

;(defmacro html
;  "Efficiently compile a Clojure data structure into a list of HTML."
;  [& content]
;  `(list ~@(mapcat compile-html content)))
