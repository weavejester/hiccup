(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:use hiccup.compile
        hiccup.util)
  (:import java.net.URI))

(def h escape-html)  ; alias for escape-html

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (if-let [mode (and (map? options) (:mode options))]
    (binding [*html-mode* mode]
      `(binding [*html-mode* ~mode]
         ~(apply compile-html content)))
    (apply compile-html options content)))

(defmacro defhtml
  "Define a function, but wrap its output in an implicit html macro."
  [name & fdecl]
  (let [[fhead fbody] (split-with #(not (or (list? %) (vector? %))) fdecl)
        wrap-html (fn [[args & body]] `(~args (html ~@body)))]
    `(defn ~name
       ~@fhead
       ~@(if (vector? (first fbody))
           (wrap-html fbody)
           (map wrap-html fbody)))))

(defn add-optional-attrs
  "Add an optional attribute argument to a function that returns a vector tag."
  [func]
  (fn [& args]
    (if (map? (first args))
      (let [[tag & body] (apply func (rest args))]
        (if (map? (first body))
          (apply vector tag (merge (first body) (first args)) (rest body))
          (apply vector tag (first args) body)))
      (apply func args))))

(defmacro defelem
  "Defines a function that will return a tag vector. If the first argument
  passed to the resulting function is a map, it merges it with the attribute
  map of the returned tag value."
  [name & fdecl]
  `(do (defn ~name ~@fdecl)
       (alter-var-root (var ~name) add-optional-attrs)))

(def ^:dynamic *base-url* nil)

(defmacro with-base-url
  "Add a base-url that will be added to the output of the resolve-uri function."
  [base-url & body]
  `(binding [*base-url* ~base-url]
     ~@body))

(defn resolve-uri
  "Prepends the base-url to the supplied URI."
  [uri]
  (if (.isAbsolute (URI. uri))
    uri
    (str *base-url* uri)))
