(ns hiccup.util
  "Utility functions for Hiccup."
  (:require [clojure.string :as str])
  (:import java.net.URI
           java.net.URLEncoder))

(def ^:dynamic *base-url* nil)

(defmacro with-base-url
  "Sets a base URL that will be prepended onto relative URIs. Note that for this
  to work correctly, it needs to be placed outside the html macro."
  [base-url & body]
  `(binding [*base-url* ~base-url]
     ~@body))

(defprotocol ToString
  (^String to-str [x] "Convert a value into a string."))

(extend-protocol ToString
  clojure.lang.Keyword
  (to-str [k] (name k))
  java.net.URI
  (to-str [u]
    (if (or (.isAbsolute u)
            (not (-> (.getPath u) (.startsWith "/"))))
      (str u)
      (str *base-url* u)))
  Object
  (to-str [x] (str x))
  nil
  (to-str [_] ""))

(defn ^String as-str
  "Converts its arguments into a string using to-str."
  [& xs]
  (apply str (map to-str xs)))

(defprotocol ToURI
  (^URI to-uri [x] "Convert a value into a URI."))

(extend-protocol ToURI
  java.net.URI
  (to-uri [u] u)
  String
  (to-uri [s] (URI. s)))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. ^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")))

(def ^:dynamic *encoding* "UTF-8")

(defmacro with-encoding
  "Sets a default encoding for URL encoding strings. Defaults to UTF-8."
  [encoding & body]
  `(binding [*encoding* ~encoding]
     ~@body))

(defprotocol URLEncode
  (url-encode [x] "Turn a value into a URL-encoded string."))

(extend-protocol URLEncode
  String
  (url-encode [s] (URLEncoder/encode s *encoding*))
  java.util.Map
  (url-encode [m]
    (str/join "&"
      (for [[k v] m]
        (str (url-encode k) "=" (url-encode v)))))
  Object
  (url-encode [x] (url-encode (to-str x))))

(defn url
  "Creates a URL string from a variable list of arguments and an optional
  parameter map as the last argument. For example:
    (url \"/group/\" 4 \"/products\" {:page 9})
    => \"/group/4/products?page=9\""
  [& args]
  (let [params (last args), args (butlast args)]
    (to-uri
     (str (apply str args)
          (if (map? params)
            (str "?" (url-encode params))
            params)))))

