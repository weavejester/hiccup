(ns hiccup.util
  "Utility functions for Hiccup."
  (:require [clojure.string :as str])
  #+clj (:import java.net.URI
                 java.net.URLEncoder)
  #+cljs (:import goog.Uri
                  goog.string))

(def ^:dynamic *base-url* nil)

(defmacro with-base-url
  "Sets a base URL that will be prepended onto relative URIs. Note that for this
  to work correctly, it needs to be placed outside the html macro."
  [base-url & body]
  `(binding [*base-url* ~base-url]
     ~@body))

(defprotocol ToString
  (to-str [x] "Convert a value into a string."))

#+clj
(extend-protocol ToString
  clojure.lang.Keyword
  (to-str [k] (name k))
  clojure.lang.Ratio
  (to-str [r] (str (float r)))
  java.net.URI
  (to-str [u]
    (if (or (.getHost u)
            (nil? (.getPath u))
            (not (-> (.getPath u) (.startsWith "/"))))
      (str u)
      (let [base (str *base-url*)]
        (if (.endsWith base "/")
          (str (subs base 0 (dec (count base))) u)
          (str base u)))))
  Object
  (to-str [x] (str x))
  nil
  (to-str [_] ""))

#+cljs
(extend-protocol ToString
  cljs.core.Keyword
  (to-str [x] (name x))
  goog.Uri
  (to-str [x]
    (if (or (. x (hasDomain))
            (nil? (. x (getPath)))
            (not (re-matches #"^/.*" (. x (getPath)))))
      (str x)
      (let [base (str *base-url*)]
        (if (re-matches #".*/$" base)
          (str (subs base 0 (dec (count base))) x)
          (str base x)))))
  number
  (to-str [x] (str x))
  default
  (to-str [x] (str x))
  nil
  (to-str [_] ""))

(defn as-str
  "Converts its arguments into a string using to-str."
  [& xs]
  (apply str (map to-str xs)))

(defprotocol ToURI
  (to-uri [x] "Convert a value into a URI."))

#+clj
(extend-protocol ToURI
  java.net.URI
  (to-uri [u] u)
  String
  (to-uri [s] (URI. s)))

#+cljs
(extend-protocol ToURI
  Uri
  (to-uri [x] x)
  default
  (to-uri [x] (Uri. (str x))))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  #+clj
  (.. ^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;"))
  #+cljs
  (string/htmlEscape (as-str text)))

(def ^{:doc "Alias for hiccup.util/escape-html"}
  h escape-html)

(def ^:dynamic *encoding* "UTF-8")

(defmacro with-encoding
  "Sets a default encoding for URL encoding strings. Defaults to UTF-8."
  [encoding & body]
  `(binding [*encoding* ~encoding]
     ~@body))

(defprotocol URLEncode
  (url-encode [x] "Turn a value into a URL-encoded string."))

#+clj
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

#+cljs
(extend-protocol URLEncode
  string
  (url-encode [s]
    (-> (goog.string/urlEncode s)
        (str/replace "%20" "+")))

  PersistentArrayMap
  (url-encode [m]
    (str/join "&"
      (for [[k v] m]
        (str (url-encode k) "=" (url-encode v)))))

  default
  (url-encode [x] (url-encode (to-str x))))

(defn url
  "Creates a URI instance from a variable list of arguments and an optional
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
