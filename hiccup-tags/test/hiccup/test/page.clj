(ns hiccup.test.page
  (:use clojure.test
        hiccup.page)
  (:import java.net.URI))

(deftest xhtml-tag-test
  (is (= (xhtml-tag "en-US" [:body "foo"])
         [:html {:xmlns "http://www.w3.org/1999/xhtml" "xml:lang" "en-US" :lang "en-US"}
          '([:body "foo"])])))

(deftest xml-declaration-test
  (is (= (xml-declaration "ISO-8859-1")
         "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n")))

(deftest html4-test
  (is (= (html4 [:body [:p "Hello" [:br] "World"]])
         (list (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" "
                    "\"http://www.w3.org/TR/html4/strict.dtd\">\n")
               [:html [:body [:p "Hello" [:br] "World"]]]))))

(deftest xhtml-test
  (is (= (xhtml [:body [:p "Hello" [:br] "World"]])
         (list "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
               (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
               [:html {:xmlns "http://www.w3.org/1999/xhtml" "xml:lang" nil :lang nil}
                '([:body [:p "Hello" [:br] "World"]])])))
  (is (= (xhtml {:lang "en"} [:body "Hello World"])
         (list "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
               (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
               [:html {:xmlns "http://www.w3.org/1999/xhtml"
                       "xml:lang" "en" :lang "en"}
                '([:body "Hello World"])])))
  (is (= (xhtml {:encoding "ISO-8859-1"} [:body "Hello World"])
         (list "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
               (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
               [:html {:xmlns "http://www.w3.org/1999/xhtml" "xml:lang" nil :lang nil}
                '([:body "Hello World"])]))))

(deftest html5-test
  (testing "HTML mode"
    (is (= (html5 [:body [:p "Hello" [:br] "World"]])
           '("<!DOCTYPE html>\n"
             [:html {}  [:body [:p "Hello" [:br] "World"]]])))
    (is (= (html5 {:lang "en"} [:body [:p "Hello" [:br] "World"]])
           '("<!DOCTYPE html>\n"
             [:html {:lang "en"} [:body [:p "Hello" [:br] "World"]]])))
    (is (= (html5 {:prefix "og: http://ogp.me/ns#"}
                  [:body "Hello World"])
           '("<!DOCTYPE html>\n"
             [:html {:prefix "og: http://ogp.me/ns#"}
              [:body "Hello World"]])))
    (is (= (html5 {:prefix "og: http://ogp.me/ns#"
                   :lang "en"}
                  [:body "Hello World"])
           '("<!DOCTYPE html>\n"
             [:html {:prefix "og: http://ogp.me/ns#" :lang "en"}
              [:body "Hello World"]]))))
  (testing "XML mode"
    (is (= (html5 {:xml? true} [:body [:p "Hello" [:br] "World"]])
           '("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             "<!DOCTYPE html>\n"
             [:html {:xmlns "http://www.w3.org/1999/xhtml" "xml:lang" nil :lang nil}
              ([:body [:p "Hello" [:br] "World"]])])))
    (is (= (html5 {:xml? true :lang "en"} [:body "Hello World"])
           '("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             "<!DOCTYPE html>\n"
             [:html {:xmlns "http://www.w3.org/1999/xhtml", "xml:lang" "en", :lang "en"}
              ([:body "Hello World"])])))
    (is (= (html5 {:xml? true
                   "xml:og" "http://ogp.me/ns#"} [:body "Hello World"])
           '("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             "<!DOCTYPE html>\n"
             [:html {"xml:og" "http://ogp.me/ns#", :xmlns "http://www.w3.org/1999/xhtml", "xml:lang" nil, :lang nil}
              ([:body "Hello World"])])))
    (is (= (html5 {:xml? true :lang "en"
                   "xml:og" "http://ogp.me/ns#"} [:body "Hello World"])
           '("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             "<!DOCTYPE html>\n"
             [:html {"xml:og" "http://ogp.me/ns#", :xmlns "http://www.w3.org/1999/xhtml", "xml:lang" "en", :lang "en"}
              ([:body "Hello World"])])))))

(deftest include-js-test
  (is (= (include-js "foo.js")
         (list [:script {:type "text/javascript" :src (URI. "foo.js")}])))
  (is (= (include-js "foo.js" "bar.js")
         (list [:script {:type "text/javascript" :src (URI. "foo.js")}]
               [:script {:type "text/javascript" :src (URI. "bar.js")}]))))

(deftest include-css-test
  (is (= (include-css "foo.css")
         (list [:link {:type "text/css" :href (URI. "foo.css") :rel "stylesheet"}])))
  (is (= (include-css "foo.css" "bar.css")
         (list [:link {:type "text/css" :href (URI. "foo.css") :rel "stylesheet"}]
               [:link {:type "text/css" :href (URI. "bar.css") :rel "stylesheet"}]))))
