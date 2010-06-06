(ns hiccup.test.page-helpers
  (:use clojure.test
        hiccup.page-helpers))

(deftest encode-params-test
  (are [m s] (= (encode-params m) s)
    {"a" "b"}       "a=b"
    {:a "b"}        "a=b"
    {:a "b" :c "d"} "a=b&c=d"
    {:a "&"}        "a=%26"))

(deftest url-test
  (are [u s] (= u s)
    (url "foo")          "foo"
    (url "foo/" 1)       "foo/1"
    (url "/foo/" "bar")  "/foo/bar"
    (url {:a "b"})       "?a=b"
    (url "foo" {:a "&"}) "foo?a=%26"
    (url "/foo/" 1 "/bar" {:page 2}) "/foo/1/bar?page=2"))
