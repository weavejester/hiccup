(ns hiccup.test.util
  (:use clojure.test
        hiccup.util))

(deftest test-escaped-chars
  (is (= (escape-html "\"") "&quot;"))
  (is (= (escape-html "<") "&lt;"))
  (is (= (escape-html ">") "&gt;"))
  (is (= (escape-html "&") "&amp;"))
  (is (= (escape-html "foo") "foo")))

(deftest test-as-str
  (is (= (as-str "foo") "foo"))
  (is (= (as-str :foo) "foo"))
  (is (= (as-str 100) "100"))
  (is (= (as-str "a" :b 3) "ab3")))