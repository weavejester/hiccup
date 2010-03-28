(ns test.hiccup.core-test
  (:use clojure.test)
  (:use hiccup.core))

(deftest tag-text
  (is (= (html [:text "Lorem Ipsum"]) "<text>Lorem Ipsum</text>")))

(deftest empty-tags
  (is (= (html [:text]) "<text />")))

(deftest empty-block-tags
  (is (= (html [:div]) "<div></div>"))
  (is (= (html [:h1]) "<h1></h1>"))
  (is (= (html [:script]) "<script></script>")))

(deftest empty-links-tag
  (is (= (html [:a]) "<a></a>")))

(deftest tags-can-be-strs
  (is (= (html ["div"] "<div></div>"))))

(deftest tags-can-be-symbols
  (is (= (html ['div] "<div></div>"))))

(deftest tag-concatenation
  (is (= (html [:body "foo" "bar"]) "<body>foobar</body>"))
  (is (= (html [:body [:p] [:br]])) "<body><p /><br /></body>"))

(deftest tag-seq-expand
  (is (= (html [:body (list "foo" "bar")])
         "<body>foobar</body>")))

(deftest html-seq-expand
  (is (= (html (list [:p "a"] [:p "b"]))
         "<p>a</p><p>b</p>")))

(deftest nested-tags
  (is (= (html [:div [:p]] "<div><p /></div>")))
  (is (= (html [:div [:b]] "<div><b></b></div>")))
  (is (= (html [:p [:span [:a "foo"]]])
         "<p><span><a>foo</a></span></p>")))

(deftest attribute-maps
  (is (= (html [:xml {:a "1", :b "2"}])
         "<xml a=\"1\" b=\"2\" />")))

(deftest blank-attribute-map
  (is (= (html [:xml {}]) "<xml />")))

(deftest escaped-chars
  (is (= (escape-html "\"") "&quot;"))
  (is (= (escape-html "<") "&lt;"))
  (is (= (escape-html ">") "&gt;"))
  (is (= (escape-html "&") "&amp;"))
  (is (= (escape-html "foo") "foo")))

(deftest escaped-attrs
  (is (= (html [:div {:id "\""}])
         "<div id=\"&quot;\"></div>")))

(deftest attrs-can-be-strs
  (is (= (html [:img {"id" "foo"}]) "<img id=\"foo\" />")))

(deftest attrs-can-be-symbols
  (is (= (html [:img {'id "foo"}]) "<img id=\"foo\" />")))

(deftest attr-keys-different-types
  (is (= (html [:xml {:a "1", 'b "2", "c" "3"}])
         "<xml a=\"1\" b=\"2\" c=\"3\" />")))

(deftest tag-class-sugar
  (is (= (html [:div.foo]) "<div class=\"foo\"></div>"))
  (is (= (html [:div.foo (str "bar" "baz")])
         "<div class=\"foo\">barbaz</div>")))

(deftest tag-many-class-sugar
  (is (= (html [:div.a.b]) "<div class=\"a b\"></div>"))
  (is (= (html [:div.a.b.c]) "<div class=\"a b c\"></div>")))

(deftest tag-id-sugar
  (is (= (html [:div#foo]) "<div id=\"foo\"></div>")))

(deftest tag-id-and-classes
  (is (= (html [:div#foo.bar.baz])
         "<div class=\"bar baz\" id=\"foo\"></div>")))

(deftest attrs-bool-true
  (is (= (html [:input {:type "checkbox" :checked true}])
         "<input checked=\"checked\" type=\"checkbox\" />")))

(deftest attrs-bool-false
  (is (= (html [:input {:type "checkbox" :checked false}])
         "<input type=\"checkbox\" />")))

(deftest attrs-nil
  (is (= (html [:span {:class nil} "foo"])
         "<span>foo</span>")))

(deftest attrs-are-evaluated
  (is (= (html [:img {:src (str "/foo" "/bar")}])
         "<img src=\"/foo/bar\" />"))
  (is (= (html [:div {:id (str "a" "b")} (str "foo")])
         "<div id=\"ab\">foo</div>")))

(deftest content-can-be-vars
  (is (= (let [x "foo"] (html [:span x]))
         "<span>foo</span>")))

(deftest optimized-forms
  (is (= (html [:ul (for [n (range 3)]
                      [:li n])])
         "<ul><li>0</li><li>1</li><li>2</li></ul>"))
  (is (= (html [:div (if true
                       [:span "foo"]
                       [:span "bar"])])
         "<div><span>foo</span></div>")))

(deftest type-hints
  (let [string "x", number 1]
    (is (= (html [:span #^String string]) "<span>x</span>"))
    (is (= (html [:span #^Integer number]) "<span>1</span>"))))

(deftest mode-ending-tag
  (is (= (html [:br]) "<br />"))
  (is (= (html {:mode :xml} [:br]) "<br />"))
  (is (= (html {:mode :sgml} [:br]) "<br>"))
  (is (= (html {:mode :html} [:br]) "<br>")))
