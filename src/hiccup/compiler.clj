(ns hiccup.compiler
  (:refer-clojure :exclude (compile))
  (:require [clojure.walk :as walk]))

(declare render)

(defn render-vector [[tag & [attrs & content :as tail]]]
  (if (map? attrs)
    {:tag tag, :attrs attrs, :content (render content)}
    {:tag tag, :attrs {}, :content (render tail)}))

(defn render [node]
  (cond
   (vector? node) (render-vector node)
   (seq? node)    (map render node)
   :otherwise     node))
