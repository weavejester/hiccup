(ns test.hiccup.form-helpers
  (:use clojure.contrib.test-is)
  (:use hiccup)
  (:use hiccup.form-helpers))

(deftest test-hidden-field
  (is (= (html (hidden-field :foo "bar"))
         "<input id=\"foo\" name=\"foo\" type=\"hidden\" value=\"bar\" />")))

(deftest test-text-field
  (is (= (html (text-field :foo))
         "<input id=\"foo\" name=\"foo\" type=\"text\" />")))

(deftest test-check-box
  (is (= (html (check-box :foo true))
         (str "<input checked=\"checked\" id=\"foo\" name=\"foo\""
              " type=\"checkbox\" value=\"true\" />"))))
