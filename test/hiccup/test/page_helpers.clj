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

(deftest html4-test
  (is (= (html4 [:body [:p "Hello" [:br] "World"]])
         (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" "
              "\"http://www.w3.org/TR/html4/strict.dtd\">\n"
              "<html><body><p>Hello<br>World</p></body></html>"))))
