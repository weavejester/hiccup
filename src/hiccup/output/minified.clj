(ns hiccup.output.minified
  "Renders a HTML DOM into minified HTML."
  (:use [hiccup.util :only (as-str escape-html)]))

(def ^{:doc "A list of tags that need an explicit ending tag when rendered."
       :private true}
  container-tags
  #{"a" "article" "aside" "b" "body" "canvas" "dd" "div" "dl" "dt" "em" "fieldset"
    "footer" "form" "h1" "h2" "h3" "h4" "h5" "h6" "head" "header" "hgroup" "html"
    "i" "iframe" "label" "li" "nav" "ol" "option" "pre" "section" "script" "span"
    "strong" "style" "table" "textarea" "title" "ul"})

(defn to-string
  ([n & nodes]
     (apply str (map to-string (cons n nodes))))
  ([node]
     (with-out-str
       (print (str "<" (as-str (:tag node))))
       (doseq [[k v] (:attrs node)]
         (when v
           (print (str " " (as-str k) "=\"" (escape-html v) "\""))))
       (print ">")
       (doseq [content (:content node)]
         (if (map? content)
           (to-string content)
           (print (escape-html content))))
       (print (str "</" (as-str (:tag node)) ">")))))
