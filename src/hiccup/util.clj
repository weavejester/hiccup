(ns hiccup.util
  "Utility functions for Hiccup."
  (:import java.net.URI))

(defn as-str
  "Convert an object into a string."
  [x]
  (if (instance? clojure.lang.Named x)
    (name x)
    (str x)))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. ^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def ^:dynamic *base-url* nil)

(defmacro with-base-url
  "Add a base-url that will be added to the output of the resolve-uri function."
  [base-url & body]
  `(binding [*base-url* ~base-url]
     ~@body))

(defn resolve-uri
  "Prepends the base-url to the supplied URI."
  [uri]
  (if (.isAbsolute (URI. uri))
    uri
    (str *base-url* uri)))
