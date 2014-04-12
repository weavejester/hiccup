(ns hiccup.test.middleware
  (:use clojure.test
        hiccup.middleware
        hiccup.util))

(defn test-handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str *base-url* "/bar")})

(deftest test-wrap-base-url
  (let [resp ((wrap-base-url test-handler "/foo") {})]
    (is (= (:body resp)
           "/foo/bar"))))
