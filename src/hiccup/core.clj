(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:require [hiccup.compiler :as compiler]
            [hiccup.util :as util]))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (if (map? options)
    (let [mode            (:mode options :xhtml)
          escape-strings? (:escape-strings? options true)]
      `(binding [util/*html-mode*       ~mode
                 util/*escape-strings?* ~escape-strings?]
         (util/raw-string ~(apply compiler/compile-html-with-bindings content))))
    `(util/raw-string ~(apply compiler/compile-html-with-bindings options content))))

(defn h
  "Escape strings when the :escape-strings? option is false."
  [text]
  (if util/*escape-strings?*
    (util/as-str text)
    (util/escape-html text)))

(def raw
  "Alias for hiccup.util/raw-string."
  util/raw-string)
