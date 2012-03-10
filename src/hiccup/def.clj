(ns hiccup.def
  "Macros for defining functions that generate HTML"
  (:use hiccup.core))

(defmacro defhtml
  "Define a function, but wrap its output in an implicit html macro."
  [name & fdecl]
  (let [[fhead fbody] (split-with #(not (or (list? %) (vector? %))) fdecl)
        wrap-html     (fn [[args & body]] `(~args (html ~@body)))]
    `(defn ~name
       ~@fhead
       ~@(if (vector? (first fbody))
           (wrap-html fbody)
           (map wrap-html fbody)))))

(defn wrap-attrs
  "Add an optional attribute argument to a function that returns a vector tag."
  [func]
  (fn [& args]
    (if (map? (first args))
      (let [[tag & body] (apply func (rest args))]
        (if (map? (first body))
          (apply vector tag (merge (first body) (first args)) (rest body))
          (apply vector tag (first args) body)))
      (apply func args))))

(defn ^:private add-attrs-meta [m]
  (update-in m [:arglists] #(for [arglist %]
                              (vec (cons 'attrs arglist)))))

(defmacro defelem
  "Defines a function that will return a tag vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned tag value."
  [name & fdecl]
  `(do (defn ~name ~@fdecl)
       (alter-meta! (var ~name) #'add-attrs-meta)
       (alter-var-root (var ~name) wrap-attrs)))
