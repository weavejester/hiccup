(ns hiccup.test.element
  (:use clojure.test
        hiccup.element)
  (:import java.net.URI))

(deftest javascript-tag-test
  (is (= (javascript-tag "alert('hello');")
         [:script {:type "text/javascript"}
          "//<![CDATA[\nalert('hello');\n//]]>"])))

(deftest link-to-test
  (is (= (link-to "/")
         [:a {:href (URI. "/")} nil]))
  (is (= (link-to "/" "foo")
         [:a {:href (URI. "/")} (list "foo")]))
  (is (= (link-to "/" "foo" "bar")
         [:a {:href (URI. "/")} (list "foo" "bar")])))

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

