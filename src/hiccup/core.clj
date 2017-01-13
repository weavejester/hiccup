(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:require [hiccup2.core :as hiccup2]
            [hiccup.util :as util]))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (if (map? options)
    `(str (hiccup2/html ~(assoc options :escape-strings? false) ~@content))
    `(str (hiccup2/html {:escape-strings? false} ~options ~@content))))

(defn h
  "Escape strings when the :escape-strings? option is false."
  [text]
  (if util/*escape-strings?*
    (util/as-str text)
    (util/escape-html text)))
