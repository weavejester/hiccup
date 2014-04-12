(ns hiccup.test.def
  (:use clojure.test
        hiccup.def))

(deftest test-defhtml
  (testing "basic html function"
    (defhtml basic-fn [x] [:span x])
    (is (= (basic-fn "foo") "<span>foo</span>")))
  (testing "html function with overloads"
    (defhtml overloaded-fn
      ([x] [:span x])
      ([x y] [:span x [:div y]]))
    (is (= (overloaded-fn "foo") "<span>foo</span>"))
    (is (= (overloaded-fn "foo" "bar")
           "<span>foo<div>bar</div></span>"))))

(deftest test-defelem
  (testing "one overload function"
    (defelem one-form-two-args [a b] [b a 3])
    (is (thrown? IllegalArgumentException (one-form-two-args)))
    (is (thrown? IllegalArgumentException (one-form-two-args {})))
    (is (thrown? IllegalArgumentException (one-form-two-args 1)))
    (is (= [1 0 3] (one-form-two-args 0 1)))
    (is (= [1 {:foo :bar} 0 3] (one-form-two-args {:foo :bar} 0 1)))
    (is (thrown? IllegalArgumentException (one-form-two-args 1 2 3)))
    (is (thrown? IllegalArgumentException (one-form-two-args 1 2 3 4))))
  (testing "three overloads function"
    (defelem three-forms
      ([] [0])
      ([a] [(* a a) 2])
      ([a b] [b a]))
    (is (= [0] (three-forms)))
    (is (= [0 {:foo :bar}] (three-forms {:foo :bar})))
    (is (= [4 2] (three-forms 2)))
    (is (= [4 {:foo :bar} 2] (three-forms {:foo :bar} 2)))
    (is (= [1 0] (three-forms 0 1)))
    (is (= [1 {:foo :bar} 0] (three-forms {:foo :bar} 0 1)))
    (is (thrown? IllegalArgumentException (three-forms 1 2 3)))
    (is (thrown? IllegalArgumentException (three-forms 1 2 3 4))))
  (testing "recursive function"
    (defelem recursive [a]
      (if (< a 1) [a (inc a)] (recursive (- a 1))))
    (is (= [0 1] (recursive 4)))
    (is (= [0 {:foo :bar} 1] (recursive {:foo :bar} 4))))
  (testing "merge map if result has one"
    (defelem with-map
      ([] [1 {:foo :bar} 2])
      ([a b] [a {:foo :bar} b]))
    (is (= [1 {:foo :bar} 2] (with-map)))
    (is (= [1 {:a :b :foo :bar} 2] (with-map {:a :b})))
    (is (= [1 {:foo :bar} 2] (with-map 1 2)))
    (is (= [1 {:foo :bar :a :b} 2] (with-map {:a :b} 1 2))))
  (testing "preserve meta info"
    (defelem three-forms-extra
      "my documentation"
      {:my :attr}
      ([] {:pre [false]} [0])
      ([a] {:pre [(> a 1)]} [1])
      ([a b] {:pre [(> a 1)]} [1 2]))
    (is (thrown? AssertionError (three-forms-extra)))
    (is (thrown? AssertionError (three-forms-extra 0)))
    (is (thrown? AssertionError (three-forms-extra 0 0)))
    (is (= "my documentation" (:doc (meta #'three-forms-extra))))
    (is (= :attr (:my (meta #'three-forms-extra))))))
