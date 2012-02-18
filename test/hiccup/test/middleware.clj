(ns hiccup.test.middleware
  (:use clojure.test
        hiccup.middleware
        hiccup.core
        hiccup.element))

(defn test-handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (html [:html [:body (link-to "/bar" "bar")]])})

(deftest test-wrap-base-url
  (let [resp ((wrap-base-url test-handler "/foo") {})]
    (is (= (:body resp)
           "<html><body><a href=\"/foo/bar\">bar</a></body></html>"))))