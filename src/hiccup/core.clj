(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:use hiccup.compiler
        hiccup.util))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (if-let [mode (and (map? options) (:mode options))]
    (binding [*html-mode* mode]
      `(binding [*html-mode* ~mode]
         (raw-string ~(apply compile-html content))))
    `(raw-string ~(apply compile-html options content))))

