(ns hiccup.test.jquery
  (:use clojure.test
        hiccup.jquery)
  (:import [java.net URL HttpURLConnection]))

(deftest test-jquery-links
  (testing "Fetching links (will fail without a network connection)"
    (let [[tag {src :src}] (jquery-link)]
      (is (= (-> (URL. src) (.openConnection) (.getResponseCode)) 200)))
    (let [[tag {src :src}] (jquery-ui-link)]
      (is (= (-> (URL. src) (.openConnection) (.getResponseCode)) 200)))))
