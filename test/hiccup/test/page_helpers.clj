(ns hiccup.test.page-helpers
  (:use clojure.test
        hiccup.page-helpers))

(deftest encode-params-test
  (are [m s] (= (encode-params m) s)
    {"a" "b"}       "a=b"
    {:a "b"}        "a=b"
    {:a "b" :c "d"} "a=b&c=d"
    {:a "&"}        "a=%26"))
