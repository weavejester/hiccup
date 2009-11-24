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
  (:import clojure.lang.ISeq))

(defn escape-html
  "Change special characters into HTML character entities."
  [string]
  (.. (as-str string)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def h escape-html)   ;; alias for escape-html

(defn- format-attr
  "Turn a name/value pair into an attribute stringp"
  [name value]
  (str " " (as-str name) "=\"" (escape-html value) "\""))

(defn- make-attrs
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

(defn parse-tag-name
  "Parse the id and classes from a tag name."
  [tag]
  (rest (re-matches re-tag (as-str tag))))

(defmulti render-html
  "Render a Clojure data structure to a string of HTML."
  type)

(defmethod render-html IPersistentVector
  [[tag & content]]
  (let [[tag id class]  (parse-tag-name tag)
        tag-attrs       {:id id
                         :class (if class (.replace class "." " "))}
        map-attrs       (first content)
        [attrs content] (if (map? map-attrs)
                          [(merge tag-attrs map-attrs) (next content)]
                          [tag-attrs content])]
    (if (or content (container-tags tag))
      (str "<" tag (make-attrs attrs) ">"
           (render-html content)
           "</" tag ">")
      (str "<" tag (make-attrs attrs) " />"))))

(defmethod render-html ISeq
  [coll]
  (apply str (map render-html coll)))

(defmethod render-html :default [x]
  (as-str x))

(defn html
  "Render Clojure data structures to HTML."
  [& content]
  (render-html content))
