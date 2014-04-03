(ns hiccup.test.element
  #+clj (:use clojure.test
              hiccup.element
              hiccup.util)
  #+cljs (:require [cemerick.cljs.test :refer-macros [is deftest]]
                   [hiccup.element :refer [javascript-tag link-to mail-to
                                           unordered-list ordered-list]]
                   [hiccup.util :refer [to-uri]]))

(deftest javascript-tag-test
  (is (= (javascript-tag "alert('hello');")
         [:script {:type "text/javascript"}
          "//<![CDATA[\nalert('hello');\n//]]>"])))

(deftest link-to-test
  (is (= (link-to "/")
         [:a {:href (to-uri "/")} nil]))
  (is (= (link-to "/" "foo")
         [:a {:href (to-uri "/")} '("foo")]))
  (is (= (link-to "/" "foo" "bar")
         [:a {:href (to-uri "/")} '("foo" "bar")])))

(deftest mail-to-test
  (is (= (mail-to "foo@example.com")
         [:a {:href "mailto:foo@example.com"} "foo@example.com"]))
  (is (= (mail-to "foo@example.com" "foo")
         [:a {:href "mailto:foo@example.com"} "foo"])))

(deftest unordered-list-test
  (is (= (unordered-list ["foo" "bar" "baz"])
         [:ul (list [:li "foo"]
                    [:li "bar"]
                    [:li "baz"])])))

(deftest ordered-list-test
  (is (= (ordered-list ["foo" "bar" "baz"])
         [:ol (list [:li "foo"]
                    [:li "bar"]
                    [:li "baz"])])))
