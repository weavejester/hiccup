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
  (apply str
    (for [[attr value] (sort (map format-attr attrs))]
      (if attr
        (str " " attr "=\"" value "\"")))))

(defvar- re-tag
  #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?"
  "Regular expression that parses a CSS-style id and class from a tag name.")

(defn make-start-tag
  "Create the start of a tag, given a tag name with optional CSS-style syntax
  to denote the id and classes."
  [tag]
  (let [[_ tag id classes] (re-matches re-tag (as-str tag))]
    (str "<" tag (make-attrs {:id id, :class classes}))))

(defn make-tag-attrs
  "Create the attributes for a tag, if the second element of the vector is a
  map."
  [attrs]
  (if (map? attrs)
    (make-attrs attrs)
    ""))

(defn literal?
  "True if the object is a literal string, keyword, number, map, vector or
  quoted object."
  [x]
  (or (string? x)
      (number? x)
      (keyword? x)
      (vector? x)
      (map? x)
      (and (list? x)
           (= (first x) 'quote))))

(defn blank?
  "True if its argument is nil or a blank string"
  [s]
  (or (nil? s) (= s "")))

(defmacro concat-strs
  "An way of concatenating strings that precompiles functions with literal
  arguments for efficiency."
  [& forms]
  (let [buffer (gensym "sb")]
   `(let [~buffer (StringBuffer.)]
     ~@(remove nil?
         (for [form forms]
           (if (or (literal? form) (every? literal? (rest form)))
             (let [value (eval form)]
               (if-not (blank? value)
                 `(.append ~buffer ~value)))
            `(.append ~buffer ~form))))
       (.toString ~buffer))))

(defmacro html-tag
  "Efficiently create a HTML tag."
  [tag & content]
  (concat-strs
    (make-start-tag tag)
    (make-tag-attrs (first content))
    ">"))
