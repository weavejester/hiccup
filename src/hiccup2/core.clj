(ns hiccup2.core
  "Library for rendering a tree of vectors into HTML. Pre-compiles where
  possible for performance. Strings are automatically escaped."
  (:require [hiccup.compiler :as compiler]
            [hiccup.util :as util]))

(defmacro html
  "Render Clojure data structures to a compiled representation of HTML. To turn
  the representation into a string, use clojure.core/str. Strings inside the
  macro are automatically HTML-escaped. To insert a string without it being
  escaped, use the raw function.

  A literal option map may be specified as the first argument. It accepts two
  keys that control how the HTML is outputted:

    :mode            - one of :html, :xhtml, :xml or :sgml (defaults to :xhtml)
    :escape-strings? - true if strings should be escaped (defaults to true)"
  [options & content]
  (if (map? options)
    (let [mode            (:mode options :xhtml)
          escape-strings? (:escape-strings? options true)]
      `(binding [util/*html-mode*       ~mode
                 util/*escape-strings?* ~escape-strings?]
         (util/raw-string ~(apply compiler/compile-html-with-bindings content))))
    `(util/raw-string ~(apply compiler/compile-html-with-bindings options content))))

(def raw
  "Short alias for hiccup.util/raw-string."
  util/raw-string)
