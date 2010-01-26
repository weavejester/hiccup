;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns hiccup.compiler
  "Optimizes tag vectors into S-expressions when possible."
  (:use hiccup.renderer))

(defmacro match-pattern
  "Match and destructure the supplied pattern."
  [target pattern & body]
  (let [binds (map #(if (seq? %) (last %) %) pattern)
        preds (remove symbol? pattern)
        target-sym (gensym target)
        length-eq? (if-not (contains? (set pattern) '&)
                    `((= (count ~target-sym) ~(count pattern))))]
    `(let [~target-sym ~target, [~@binds] ~target-sym]
       (when (and ~@length-eq? ~@preds)
         ~@body))))

(defmacro case-pattern
  "A case statement that employs match-pattern."
  [target & clauses]
  `(or ~@(for [[pattern body] (partition 2 clauses)]
           `(match-pattern ~target ~pattern ~body))))

(defn lit?
  "True if x is a literal value that can be rendered as-is."
  [x]
  (not (or (symbol? x)
           (and (vector? x)
                (not (every? lit? x)))
           (and (seq? x)
                (not= (first x) `quote)))))

(defn compile-tag [element]
  (case-pattern element
    [& (every? lit? content)]
      (render-tag (eval element))
    [& content]
      `(render-tag
         [~@(for [x content]
              (if (vector? x)
                (compile-tag x)
                x))])))
