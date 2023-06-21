(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  {:deprecated "2.0"}
  (:require [hiccup2.core :as hiccup2]
            [hiccup.util :as util]))

(defmacro html
  "Render Clojure data structures to a string of HTML. Strings are **not**
  automatically escaped, but must be manually escaped with the [[h]] function.

  A literal option map may be specified as the first argument. It accepts the
  following keys:

  `:mode`
  : One of `:html`, `:xhtml`, `:xml` or `:sgml` (defaults to `:xhtml`).
    Controls how tags are rendered."
  {:deprecated "2.0"}
  [options & content]
  (if (map? options)
    `(str (hiccup2/html ~(assoc options :escape-strings? false) ~@content))
    `(str (hiccup2/html {:escape-strings? false} ~options ~@content))))

(def ^{:deprecated 2.0} h
  "Escape strings within the [[html]] macro."
  util/escape-html)
