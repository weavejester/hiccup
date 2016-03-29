(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:require [hiccup.compiler :as compiler]
            [hiccup.util :as util]))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (if-let [mode (and (map? options) (:mode options))]
    (binding [util/*html-mode* mode]
      `(binding [util/*html-mode* ~mode]
         (util/raw-string ~(apply compiler/compile-html content))))
    `(util/raw-string ~(apply compiler/compile-html options content))))

(def ^{:deprecated "2.0"} h
  "Included for compatibility with Hiccup 1.0. Now just n alias for
  hiccup.util/as-str."
  util/as-str)
