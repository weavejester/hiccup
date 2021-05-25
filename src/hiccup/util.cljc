(ns hiccup.util
  "Utility functions for Hiccup."
  (:require [clojure.string :as str])
  #?(:clj (:import java.net.URI
                   java.net.URLEncoder)
     :clje (:import clojerl.String)))

(def ^:dynamic ^:no-doc *html-mode* :xhtml)

(def ^:dynamic ^:no-doc *escape-strings?* true)

(def ^:dynamic ^:no-doc *base-url* nil)

(def ^:dynamic ^:no-doc *encoding* "UTF-8")

(defmacro with-base-url
  "Sets a base URL that will be prepended onto relative URIs. Note that for this
  to work correctly, it needs to be placed outside the [[hiccup.core/html]] or
  [[hiccup2.core/html]] macros."
  [base-url & body]
  `(binding [*base-url* ~base-url]
     ~@body))

(defprotocol ToString
  (^String to-str [x] "Convert a value into a string."))

#?(:clj
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

   :clje
   (extend-protocol ToString
     clojerl.Keyword
     (to-str [k] (name k))
     default
     (to-str [x] (str x))
     nil
     (to-str [_] "")))

(defn ^String as-str
  "Converts its arguments into a string using [[to-str]]."
  [& xs]
  (apply str (map to-str xs)))

#?(:clj
  (defprotocol ToURI
    (^java.net.URI to-uri [x] "Convert a value into a URI."))
  :clje
  (defprotocol ToURI
    (^clojerl.String to-uri [x] "Convert a value into a URI.")))

#?(:clj
   (extend-protocol ToURI
     java.net.URI
     (to-uri [u] u)
     String
     (to-uri [s] (URI. s)))
   :clje
   (extend-protocol ToURI
     String
     (to-uri [s] s)))

#?(:clj
   (deftype RawString [^String s]
     Object
     (^String toString [this] s)
     (^boolean equals [this other]
      (and (instance? RawString other)
           (= s  (.toString other)))))
   :clje
   (deftype RawString [^String s]
     clojerl.IStringable
     (^String str [this] s)
     clojerl.IEquiv
     (^clojerl.Boolean equiv [this other]
      (and (instance? RawString other)
           (= s (str other))))))

(defn raw-string
  "Converts one or more strings into an object that will not be escaped when
  used with the [[hiccup2.core/html]] macro."
  {:arglists '([& xs])}
  ([] (RawString. ""))
  ([x] (RawString. (str x)))
  ([x & xs] (RawString. (apply str x xs))))

(defn raw-string?
  "Returns true if x is a RawString created by [[raw-string]]."
  [x]
  (instance? RawString x))

(defn escape-html
  "Change special characters into HTML character entities."
  [text]
  (.. ^String (as-str text)
    (replace "&"  "&amp;")
    (replace "<"  "&lt;")
    (replace ">"  "&gt;")
    (replace "\"" "&quot;")
    (replace "'" (if (= *html-mode* :sgml) "&#39;" "&apos;"))))

(defmacro with-encoding
  "Sets a default encoding for URL encoding strings. Defaults to UTF-8."
  [encoding & body]
  `(binding [*encoding* ~encoding]
     ~@body))

(defprotocol URLEncode
  (url-encode [x] "Turn a value into a URL-encoded string."))

#?(:clj
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
   :clje
   (extend-protocol URLEncode
     String
     (url-encode [s] (http_uri/encode s))
     clojerl.Map
     (url-encode [m]
       (str/join "&"
                 (for [[k v] m]
                   (str (url-encode k) "=" (url-encode v)))))
     default
     (url-encode [x] (url-encode (to-str x)))))

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
