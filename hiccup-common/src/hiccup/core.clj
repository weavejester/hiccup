(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:use hiccup.util))

(def ^{:dynamic true
       :doc "The current hiccup compiler."}
  *compiler*
  nil)

(defn resolve-compiler
  "Returns the current compiler function. Defaults to
  hiccup.compiler/compile-html, if available."
  []
  (or *compiler*
      (resolve 'hiccup.compiler/compile-html)
      (fn [& args] (throw (Exception. "hiccup.core/*compiler* not defined.")))))

(defn set-compiler!
  "Sets hiccup.core/*compiler* to f."
  [f]
  (alter-var-root #'*compiler* (constantly f)))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (if-let [mode (and (map? options) (:mode options))]
    (binding [*html-mode* mode]
      `(binding [*html-mode* ~mode]
         ~(apply (resolve-compiler) content)))
    (apply (resolve-compiler) options content)))

(def ^{:doc "Alias for hiccup.util/escape-html"}
  h escape-html)
