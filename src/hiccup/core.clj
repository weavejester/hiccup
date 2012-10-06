(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:refer-clojure :exclude (compile))
  (:use [hiccup.compiler :only (compile)]
        [hiccup.output :only (to-string)]))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [& content]
  `(to-string ~@(map compile content)))
