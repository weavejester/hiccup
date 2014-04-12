(ns hiccup.test.core
  (:use clojure.test
        hiccup.core))

(deftest test-resolve-compiler
  (testing "without a specified compiler"
    (is (thrown? Exception
                 (binding [*compiler* nil]
                   ((resolve-compiler) [:html])))))
  (testing "with specified compiler"
    (is (= (binding [*compiler* identity]
             ((resolve-compiler) [:html]))
           [:html]))))

(deftest test-set-compiler!
  (set-compiler! str)
  (is (= ((resolve-compiler) [:html])
         "[:html]")))
