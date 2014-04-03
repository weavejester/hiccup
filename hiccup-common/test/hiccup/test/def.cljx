(ns hiccup.test.def
  #+clj (:use clojure.test
              hiccup.def)
  #+cljs (:require [cemerick.cljs.test :refer-macros [deftest is testing]]
                   [hiccup.def :refer-macros [defelem]]))

(defelem one-form-two-args [a b] [b a 3])

(defelem three-forms
  ([] [0])
  ([a] [(* a a) 2])
  ([a b] [b a]))

(defelem recursive [a]
  (if (< a 1) [a (inc a)] (recursive (- a 1))))

(defelem with-map
  ([] [1 {:foo :bar} 2])
  ([a b] [a {:foo :bar} b]))

(defelem three-forms-extra
  "my documentation"
  {:my :attr}
  ([] {:pre [false]} [0])
  ([a] {:pre [(> a 1)]} [1])
  ([a b] {:pre [(> a 1)]} [1 2]))

(deftest test-defelem
  (testing "one overload function"
    #+clj (is (thrown? IllegalArgumentException (one-form-two-args)))
    #+clj (is (thrown? IllegalArgumentException (one-form-two-args {})))
    #+clj (is (thrown? IllegalArgumentException (one-form-two-args 1)))
    (is (= [1 0 3] (one-form-two-args 0 1)))
    (is (= [1 {:foo :bar} 0 3] (one-form-two-args {:foo :bar} 0 1)))
    #+clj (is (thrown? IllegalArgumentException (one-form-two-args 1 2 3)))
    #+clj (is (thrown? IllegalArgumentException (one-form-two-args 1 2 3 4))))
  (testing "three overloads function"
    (is (= [0] (three-forms)))
    (is (= [0 {:foo :bar}] (three-forms {:foo :bar})))
    (is (= [4 2] (three-forms 2)))
    (is (= [4 {:foo :bar} 2] (three-forms {:foo :bar} 2)))
    (is (= [1 0] (three-forms 0 1)))
    (is (= [1 {:foo :bar} 0] (three-forms {:foo :bar} 0 1)))
    #+clj (is (thrown? IllegalArgumentException (three-forms 1 2 3)))
    #+clj (is (thrown? IllegalArgumentException (three-forms 1 2 3 4))))
  (testing "recursive function"
    (is (= [0 1] (recursive 4)))
    (is (= [0 {:foo :bar} 1] (recursive {:foo :bar} 4))))
  (testing "merge map if result has one"
    (is (= [1 {:foo :bar} 2] (with-map)))
    (is (= [1 {:a :b :foo :bar} 2] (with-map {:a :b})))
    (is (= [1 {:foo :bar} 2] (with-map 1 2)))
    (is (= [1 {:foo :bar :a :b} 2] (with-map {:a :b} 1 2))))
  #+clj
  (testing "preserve meta info"
    (is (thrown? AssertionError (three-forms-extra)))
    (is (thrown? AssertionError (three-forms-extra 0)))
    (is (thrown? AssertionError (three-forms-extra 0 0)))
    (is (= "my documentation" (:doc (meta #'three-forms-extra))))
    (is (= :attr (:my (meta #'three-forms-extra))))))
