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

(deftest test-to-uri
  (testing "with no base URL"
    (is (= (to-str (to-uri "foo")) "foo"))
    (is (= (to-str (to-uri "/foo/bar")) "/foo/bar"))
    (is (= (to-str (to-uri "/foo#bar")) "/foo#bar")))
  (testing "with base URL"
    (with-base-url "/foo"
      (is (= (to-str (to-uri "/bar")) "/foo/bar"))
      (is (= (to-str (to-uri "http://example.com")) "http://example.com")))))
