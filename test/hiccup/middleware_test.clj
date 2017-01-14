(ns hiccup.middleware_test
  (:require [clojure.test :refer :all]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to]]
            [hiccup.middleware :refer :all]))

(defn test-handler [request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (html [:html [:body (link-to "/bar" "bar")]])})

(deftest test-wrap-base-url
  (let [resp ((wrap-base-url test-handler "/foo") {})]
    (is (= (:body resp)
           "<html><body><a href=\"/foo/bar\">bar</a></body></html>"))))
