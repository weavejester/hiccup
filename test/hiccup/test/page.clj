(ns hiccup.test.page
  (:require [fs.core :as fs])
  (:use clojure.test
        hiccup.page)
  (:import java.net.URI
           java.io.File))

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

(defn set-up-directory-structure [root extension]
  (fs/mkdir root)
  (fs/touch (str root File/separator (str "foo" extension)))
  (fs/touch (str root File/separator (str "bar" extension)))
  (fs/mkdir (str root File/separator "baz"))
  (fs/touch (str root File/separator "baz" File/separator (str "bat" extension))))

(defn tear-down-directory-structure [root extension]
  (fs/delete (str root File/separator "baz" File/separator (str "bat" extension)))
  (fs/delete (str root File/separator "baz"))
  (fs/delete (str root File/separator (str "bar" extension)))
  (fs/delete (str root File/separator (str "foo" extension)))
  (fs/delete root))

(deftest include-all-css-test
  (set-up-directory-structure "css" ".css")
  (is (= (into #{} (include-all-css))
         #{(list [:link {:type "text/css", :href (URI. "css/foo.css"), :rel "stylesheet"}])
           (list [:link {:type "text/css", :href (URI. "css/bar.css"), :rel "stylesheet"}])
           (list [:link {:type "text/css", :href (URI. "css/baz/bat.css"), :rel "stylesheet"}])}))
  (tear-down-directory-structure "css" ".css"))

(deftest include-all-js-test
  (set-up-directory-structure "js" ".js")
  (is (= (into #{} (include-all-js))         
         #{(list [:script {:type "text/javascript", :src (URI. "js/foo.js")}])
           (list [:script {:type "text/javascript", :src (URI. "js/bar.js")}])
           (list [:script {:type "text/javascript", :src (URI. "js/baz/bat.js")}])}))
  (tear-down-directory-structure "js" ".js"))