(ns hiccup.test.core
  (:use clojure.test
        hiccup.core))

(deftest tag-names
  (testing "basic tags"
    (is (= (html [:div]) "<div></div>"))
    (is (= (html ["div"]) "<div></div>"))
    (is (= (html ['div]) "<div></div>")))
  (testing "tag syntax sugar"
    (is (= (html [:div#foo]) "<div id=\"foo\"></div>"))
    (is (= (html [:div.foo]) "<div class=\"foo\"></div>"))
    (is (= (html [:div.foo (str "bar" "baz")])
           "<div class=\"foo\">barbaz</div>"))
    (is (= (html [:div.a.b]) "<div class=\"a b\"></div>"))
    (is (= (html [:div.a.b.c]) "<div class=\"a b c\"></div>"))
    (is (= (html [:div#foo.bar.baz])
           "<div class=\"bar baz\" id=\"foo\"></div>"))))

(deftest tag-contents
  (testing "empty tags"
    (is (= (html [:div]) "<div></div>"))
    (is (= (html [:h1]) "<h1></h1>"))
    (is (= (html [:script]) "<script></script>"))
    (is (= (html [:text]) "<text />"))
    (is (= (html [:a]) "<a></a>"))
    (is (= (html [:iframe]) "<iframe></iframe>"))
    (is (= (html [:title]) "<title></title>"))
    (is (= (html [:section]) "<section></section>")))
  (testing "tags containing text"
    (is (= (html [:text "Lorem Ipsum"]) "<text>Lorem Ipsum</text>")))
  (testing "contents are concatenated"
    (is (= (html [:body "foo" "bar"]) "<body>foobar</body>"))
    (is (= (html [:body [:p] [:br]]) "<body><p /><br /></body>")))
  (testing "seqs are expanded"
    (is (= (html [:body (list "foo" "bar")]) "<body>foobar</body>"))
    (is (= (html (list [:p "a"] [:p "b"])) "<p>a</p><p>b</p>")))
  (testing "vecs don't expand - error if vec doesn't have tag name"
    (is (thrown? IllegalArgumentException
                 (html (vector [:p "a"] [:p "b"])))))
  (testing "tags can contain tags"
    (is (= (html [:div [:p]]) "<div><p /></div>"))
    (is (= (html [:div [:b]]) "<div><b></b></div>"))
    (is (= (html [:p [:span [:a "foo"]]])
           "<p><span><a>foo</a></span></p>"))))

(deftest tag-attributes
  (testing "tag with blank attribute map"
    (is (= (html [:xml {}]) "<xml />")))
  (testing "tag with populated attribute map"
    (is (= (html [:xml {:a "1", :b "2"}]) "<xml a=\"1\" b=\"2\" />"))
    (is (= (html [:img {"id" "foo"}]) "<img id=\"foo\" />"))
    (is (= (html [:img {'id "foo"}]) "<img id=\"foo\" />"))
    (is (= (html [:xml {:a "1", 'b "2", "c" "3"}])
           "<xml a=\"1\" b=\"2\" c=\"3\" />")))
  (testing "attribute values are escaped"
    (is (= (html [:div {:id "\""}]) "<div id=\"&quot;\"></div>")))
  (testing "boolean attributes"
    (is (= (html [:input {:type "checkbox" :checked true}])
           "<input checked=\"checked\" type=\"checkbox\" />"))
    (is (= (html [:input {:type "checkbox" :checked false}])
           "<input type=\"checkbox\" />")))
  (testing "nil attributes"
    (is (= (html [:span {:class nil} "foo"])
           "<span>foo</span>"))))

(deftest compiled-tags
  (testing "tag content can be vars"
    (is (= (let [x "foo"] (html [:span x])) "<span>foo</span>")))
  (testing "tag content can be forms"
    (is (= (html [:span (str (+ 1 1))]) "<span>2</span>"))
    (is (= (html [:span ({:foo "bar"} :foo)]) "<span>bar</span>")))
  (testing "attributes can contain vars"
    (let [x "foo"]
      (is (= (html [:xml {:x x}]) "<xml x=\"foo\" />"))
      (is (= (html [:xml {x "x"}]) "<xml foo=\"x\" />"))
      (is (= (html [:xml {:x x} "bar"]) "<xml x=\"foo\">bar</xml>"))))
  (testing "attributes are evaluated"
    (is (= (html [:img {:src (str "/foo" "/bar")}])
           "<img src=\"/foo/bar\" />"))
    (is (= (html [:div {:id (str "a" "b")} (str "foo")])
           "<div id=\"ab\">foo</div>")))
  (testing "type hints"
    (let [string "x"]
      (is (= (html [:span ^String string]) "<span>x</span>"))))
  (testing "optimized forms"
    (is (= (html [:ul (for [n (range 3)]
                        [:li n])])
           "<ul><li>0</li><li>1</li><li>2</li></ul>"))
    (is (= (html [:div (if true
                         [:span "foo"]
                         [:span "bar"])])
           "<div><span>foo</span></div>")))
  (testing "values are evaluated only once"
    (let [times-called (atom 0)
          foo #(swap! times-called inc)]
      (html [:div (foo)])
      (is (= @times-called 1)))))

(deftest render-modes
  (testing "closed tag"
    (is (= (html [:br]) "<br />"))
    (is (= (html {:mode :xml} [:br]) "<br />"))
    (is (= (html {:mode :sgml} [:br]) "<br>"))
    (is (= (html {:mode :html} [:br]) "<br>")))
  (testing "boolean attributes"
    (is (= (html {:mode :xml} [:input {:type "checkbox" :checked true}])
           "<input checked=\"checked\" type=\"checkbox\" />"))
    (is (= (html {:mode :sgml} [:input {:type "checkbox" :checked true}])
           "<input checked type=\"checkbox\">")))
  (testing "laziness and binding scope"
    (is (= (html {:mode :sgml} [:html [:link] (list [:link])])
           "<html><link><link></html>"))))
