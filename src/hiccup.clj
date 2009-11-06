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
  (:use clojure.contrib.java-utils)
  (:import clojure.lang.IPersistentVector)
  (:import clojure.lang.IPersistentList))

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (as-str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(defn literal?
  "True if the value is a literal string, keyword, number, map, vector, nil
  or a quoted value."
  [x]
  (or (string? x)
      (number? x)
      (keyword? x)
      (vector? x)
      (map? x)
      (nil? x)
      (and (list? x)
           (= (first x) 'quote))))

(defn- pre-compile
  "Pre-compile a form if its arguments are all literals."
  [form]
  (if (every? literal? (rest form))
    (eval form)
    (list form)))

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
    (list "<" tag (make-attrs {:id id, :class classes}))))

(defn make-tag-attrs
  "Create the attributes for a tag, if the second element of the vector is a
  map."
  [attrs]
  (if (map? attrs)
    (list (make-attrs attrs))))

(defn make-end-tag
  "Create an ending tag."
  [tag]
  (list "</" (as-str tag) ">"))

(defn- remove-attrs
  "Remove optional attribute map from content."
  [content]
  (if (map? (first content))
    (rest content)
    content))

(defmulti compile-html
  "Compile a Clojure data structure value to a seq of strings and forms."
  (fn [x] (type x)))

(defmethod compile-html IPersistentVector
  [[tag & content]]
  (concat
    (pre-compile `(make-start-tag ~tag))
    (pre-compile `(make-tag-attrs ~(first content)))
    (list ">")
    (for [item (remove-attrs content)]
      (compile-html item))
    (pre-compile `(make-end-tag ~tag))))

(defmethod compile-html IPersistentList
  [form]
  form)

(defmethod compile-html :default
  [x]
  (pre-compile `(str ~x)))

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

(defn- build-str-concat
  "Build code to string concatenate a sequence of forms."
  [coll]
  (let [buffer (gensym "buffer")]
   `(let [~buffer (StringBuffer.)]
     ~@(for [x (collapse-strs coll)]
        `(.append ~buffer ~x))
       (.toString ~buffer))))

(defmacro html
  "Efficiently compile a Clojure data structure to HTML"
  [& content]
  (build-str-concat
    (for [item content]
      (compile-html item))))
