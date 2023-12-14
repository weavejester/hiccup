(ns hiccup2.optimizations-test
  (:require [clojure.test :refer :all]
            [clojure.walk :as walk]
            [hiccup2.core :as h]))

(defn- count-forms [data]
  (count (filter seq? (tree-seq coll? seq data))))

(deftest method-code-size
  ;; With Hiccup 2.0.0-RC2, it was easy to cause the hiccup2.core/html macro to
  ;; generate so much bytecode that it would go over the 64KB limit of how much
  ;; bytecode one Java method may contain. It would crash the Clojure compiler
  ;; with a "Method code too large!" exception. These are a regression tests for
  ;; that. See https://github.com/weavejester/hiccup/issues/205

  (testing "static elements should be concatenated to one string, also when they have dynamic sibling elements"
    (let [baseline     (walk/macroexpand-all
                        `(h/html [:div
                                  [:p]
                                  (identity nil)
                                  [:p]]))
          pathological (walk/macroexpand-all
                        `(h/html [:div
                                  [:p] [:p] [:p] [:p] [:p]
                                  (identity nil)
                                  [:p] [:p] [:p] [:p] [:p]]))]
      (is (= (count-forms baseline)
             (count-forms pathological)))))

  (testing "code size should grow O(n), instead of O(n^2), as more dynamic first-child elements are added"
    (let [example-0 (walk/macroexpand-all
                     `(h/html [:div
                               [:div
                                [:div
                                 [:div
                                  [:div]]]]]))
          example-1 (walk/macroexpand-all
                     `(h/html [:div (identity nil)
                               [:div
                                [:div
                                 [:div
                                  [:div]]]]]))
          example-2 (walk/macroexpand-all
                     `(h/html [:div (identity nil)
                               [:div (identity nil)
                                [:div
                                 [:div
                                  [:div]]]]]))
          example-3 (walk/macroexpand-all
                     `(h/html [:div (identity nil)
                               [:div (identity nil)
                                [:div (identity nil)
                                 [:div
                                  [:div]]]]]))
          example-4 (walk/macroexpand-all
                     `(h/html [:div (identity nil)
                               [:div (identity nil)
                                [:div (identity nil)
                                 [:div (identity nil)
                                  [:div]]]]]))
          example-5 (walk/macroexpand-all
                     `(h/html [:div (identity nil)
                               [:div (identity nil)
                                [:div (identity nil)
                                 [:div (identity nil)
                                  [:div (identity nil)]]]]]))
          examples  [example-0
                     example-1
                     example-2
                     example-3
                     example-4
                     example-5]
          diffs     (->> examples
                         (map count-forms)
                         (partition 2 1)
                         (map (fn [[a b]] (- b a))))]
      (is (< (apply max diffs)
             (* 1.2 (apply min diffs)))))))
