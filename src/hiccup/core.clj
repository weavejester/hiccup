(ns hiccup.core
  "Library for rendering a tree of vectors into a string of HTML.
  Pre-compiles where possible for performance."
  (:use hiccup.compiler
        hiccup.util))

(defn wrap-no-escape-strings [content]
  `(if-not *no-escape-strings*
     (binding [*no-escape-strings* identity-set]
       ~(apply compile-html content))
     (let [out-str# ~(apply compile-html content)]
       (set! *no-escape-strings*
             (conj *no-escape-strings* out-str#))
       out-str#)))

(defmacro html
  "Render Clojure data structures to a string of HTML."
  [options & content]
  (let [mode (and (map? options) (:mode options))
        content (if mode content (cons options content))]
    (if mode
      (binding [*html-mode* (or mode *html-mode*)]
        `(binding [*html-mode* (or ~mode *html-mode*)]
           ~(wrap-no-escape-strings content)))
      (wrap-no-escape-strings content))))

(def ^{:doc "Alias for hiccup.util/escape-html"}
  h escape-html)
