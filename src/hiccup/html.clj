(ns hiccup.html
  "Functions for setting up HTML pages."
  (:use hiccup.core)
  (:require [hiccup.page :as page]))

(defmacro html4
  "Create a HTML 4 document with the supplied contents. The first argument
  may be an optional attribute map."
  [options? & contents]
  `(html {:mode :sgml}
         (page/html4 ~options? ~@contents)))

(defmacro xhtml
  "Create a XHTML 1.0 strict document with the supplied contents. The first
  argument may be an optional attribute may. The following attributes are
  treated specially:
    :lang     - The language of the document
    :encoding - The character encoding of the document, defaults to UTF-8."
  [options? & contents]
  `(html {:mode :xml}
         (page/xhtml ~options? ~@contents)))

(defmacro html5
  "Create a HTML5 document with the supplied contents. The first argument
  may be an optional attribute map."
  [options? & contents]
  (let [mode (if (and (map? options?) (options? :xml?))
               :xml
               :html)]
     `(html {:mode ~mode}
           (page/html5 ~options? ~@contents))))
