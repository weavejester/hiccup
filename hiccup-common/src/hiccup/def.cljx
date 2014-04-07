(ns hiccup.def
  "Macros for defining functions that generate HTML"
  (:use [clojure.walk :only [postwalk-replace]]))

(defn wrap-attrs
  "Add an optional attribute argument to a function that returns a element vector."
  [func]
  (fn [& args]
    (if (map? (first args))
      (let [[tag & body] (apply func (rest args))]
        (if (map? (first body))
          (apply vector tag (merge (first body) (first args)) (rest body))
          (apply vector tag (first args) body)))
      (apply func args))))

(defn- update-arglists [arglists]
  (for [args arglists]
    (vec (cons 'attr-map? args))))

(defn add-wrap-attrs! [current-ns fname]
  #+clj (let [fvar (ns-resolve *ns* fname)]
          (alter-meta! fvar update-in [:arglists] #'update-arglists)
          (alter-var-root fvar wrap-attrs)))

(defmacro defelem
  "Defines a function that will return a element vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned element value."
  [name & fdecl]
  (let [fn-name# (gensym (str name))]
    `(if *clojure-version*
       (do (defn ~name ~@fdecl)
           (add-wrap-attrs! *ns* '~name))
       (do (defn- ~fn-name# ~@(postwalk-replace {name fn-name#} fdecl))
           (defn ~name [& args#]
             (apply (wrap-attrs ~fn-name#) args#))))))
