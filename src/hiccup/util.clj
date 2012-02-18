(ns hiccup.util
  "Utility functions for Hiccup.")

(defprotocol ToString
  (to-str [x] "Convert a value into a string."))

(extend-protocol ToString
  clojure.lang.Keyword
  (to-str [x] (name x))
  Object
  (to-str [x] (str x))
  nil
  (to-str [_] ""))

(defn as-str
  "Converts its arguments into a string using to-str."
  [& xs]
  (apply str (map to-str xs)))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. ^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))
