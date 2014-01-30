(ns hiccup.def-macros
  "Macros for defining functions that generate HTML, implemented for
  ClojureScript compatibility."
  (:require [hiccup.def]))

;; TODO: Find a way to merge this with hiccup.def/defelem
(defmacro defelem
  "Defines a function that will return a element vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned element value."
  [name & fdecl]
  (let [fn-name# (gensym (str name))]
    `(do (defn- ~fn-name# ~@fdecl)
         (def ~name (hiccup.def/wrap-attrs ~fn-name#)))))
