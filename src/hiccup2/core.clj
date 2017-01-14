(ns hiccup2.core
  "Library for rendering a tree of vectors into HTML. Pre-compiles where
  possible for performance. Strings are automatically escaped."
  {:added "2.0"}
  (:require [hiccup.compiler :as compiler]
            [hiccup.util :as util]))

(defmacro html
  "Render Clojure data structures to a compiled representation of HTML. To turn
  the representation into a string, use clojure.core/str. Strings inside the
  macro are automatically HTML-escaped. To insert a string without it being
  escaped, use the [[raw]] function.

  A literal option map may be specified as the first argument. It accepts two
  keys that control how the HTML is outputted:

  `:mode`
  : One of `:html`, `:xhtml`, `:xml` or `:sgml` (defaults to `:xhtml`).
    Controls how tags are rendered.

  `:escape-strings?`
  : True if strings should be escaped (defaults to true)."
  {:added "2.0"}
  [options & content]
  (if (map? options)
    (let [mode            (:mode options :xhtml)
          escape-strings? (:escape-strings? options true)]
      `(binding [util/*html-mode*       ~mode
                 util/*escape-strings?* ~escape-strings?]
         (util/raw-string ~(apply compiler/compile-html-with-bindings content))))
    `(util/raw-string ~(apply compiler/compile-html-with-bindings options content))))

(def ^{:added "2.0"} raw
  "Short alias for [[hiccup.util/raw-string]]."
  util/raw-string)
