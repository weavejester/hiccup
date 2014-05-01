(ns hiccup.def
  "Macros for defining functions that generate HTML")

(defn- add-attrs [[tag & body] attrs]
  (if (map? (first body))
    (apply vector tag (merge (first body) attrs) (rest body))
    (apply vector tag attrs body)))

(defn wrap-attrs
  "Add an optional attribute argument to a function that returns an element
  vector or a seq of element vectors."
  [func]
  (fn [& args]
    (if (map? (first args))
      (let [result (apply func (rest args))]
        (if (seq? result)
          (map #(add-attrs % (first args)) result)
          (add-attrs result (first args))))
      (apply func args))))

(defn- update-arglists [arglists]
  (for [args arglists]
    (vec (cons 'attr-map? args))))

(defmacro defelem
  "Defines a function that will return an element vector or a seq of element
  vectors. If the first argument passed to the resulting function is a map, it
  is merged with the attribute map of the returned element vector(s)."
  [name & fdecl]
  `(do (defn ~name ~@fdecl)
       (alter-meta! (var ~name) update-in [:arglists] #'update-arglists)
       (alter-var-root (var ~name) wrap-attrs)))
