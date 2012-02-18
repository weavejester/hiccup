(ns hiccup.test.page-helpers
  (:use clojure.test
        hiccup.page-helpers)
  (:import java.net.URI))

(deftest encode-params-test
  (are [m s] (= (encode-params m) s)
    {"a" "b"}       "a=b"
    {:a "b"}        "a=b"
    {:a "b" :c "d"} "a=b&c=d"
    {:a "&"}        "a=%26"
    {:é "è"}        "%C3%A9=%C3%A8"))

(deftest encode-params-with-encoding-test
  (are [e s] (= (with-encoding e (encode-params {:iroha "いろは"})) s)
    "UTF-8"       "iroha=%E3%81%84%E3%82%8D%E3%81%AF"
    "Shift_JIS"   "iroha=%82%A2%82%EB%82%CD"
    "EUC-JP"      "iroha=%A4%A4%A4%ED%A4%CF"
    "ISO-2022-JP" "iroha=%1B%24%42%24%24%24%6D%24%4F%1B%28%42"))

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

(deftest xhtml-test
  (is (= (xhtml [:body [:p "Hello" [:br] "World"]])
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
              "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
              "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
              "<body><p>Hello<br />World</p></body></html>")))
  (is (= (xhtml {:lang "en"} [:body "Hello World"])
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
              "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
              "<html lang=\"en\" xml:lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">"
              "<body>Hello World</body></html>")))
  (is (= (xhtml {:encoding "ISO-8859-1"} [:body "Hello World"])
         (str "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
              "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
              "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
              "<body>Hello World</body></html>"))))

(deftest html5-test
  (testing "HTML mode"
    (is (= (html5 [:body [:p "Hello" [:br] "World"]])
           "<!DOCTYPE html>\n<html><body><p>Hello<br>World</p></body></html>"))
    (is (= (html5 {:lang "en"} [:body "Hello World"])
           "<!DOCTYPE html>\n<html lang=\"en\"><body>Hello World</body></html>")))
  (testing "XML mode"
    (is (= (html5 {:xml? true} [:body [:p "Hello" [:br] "World"]])
           (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                "<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                "<body><p>Hello<br />World</p></body></html>")))
    (is (= (html5 {:xml? true, :lang "en"} [:body "Hello World"])
           (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                "<!DOCTYPE html>\n"
                "<html lang=\"en\" xml:lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">"
                "<body>Hello World</body></html>")))))

(deftest include-js-test
  (is (= (include-js "foo.js")
         (list [:script {:type "text/javascript", :src (URI. "foo.js")}])))
  (is (= (include-js "foo.js" "bar.js")
         (list [:script {:type "text/javascript", :src (URI. "foo.js")}]
               [:script {:type "text/javascript", :src (URI. "bar.js")}]))))

(deftest include-css-test
  (is (= (include-css "foo.css")
         (list [:link {:type "text/css", :href (URI. "foo.css"), :rel "stylesheet"}])))
  (is (= (include-css "foo.css" "bar.css")
         (list [:link {:type "text/css", :href (URI. "foo.css"), :rel "stylesheet"}]
               [:link {:type "text/css", :href (URI. "bar.css"), :rel "stylesheet"}]))))

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
