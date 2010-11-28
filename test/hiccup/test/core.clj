(ns hiccup.test.core
  (:use clojure.test
        clojure.contrib.mock.test-adapter
        hiccup.core))

(deftest escaped-chars
  (is (= (escape-html "\"") "&quot;"))
  (is (= (escape-html "<") "&lt;"))
  (is (= (escape-html ">") "&gt;"))
  (is (= (escape-html "&") "&amp;"))
  (is (= (escape-html "foo") "foo")))

(deftest tag-names
  (testing "basic tags"
    (is (= (html [:div] "<div></div>")))
    (is (= (html ["div"] "<div></div>")))
    (is (= (html ['div] "<div></div>"))))
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
    (is (= (html [:a]) "<a></a>")))
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
    (let [string "x", number 1]
      (is (= (html [:span ^String string]) "<span>x</span>"))
      (is (= (html [:span ^Integer number]) "<span>1</span>"))))
  (testing "optimized forms"
    (is (= (html [:ul (for [n (range 3)]
                        [:li n])])
           "<ul><li>0</li><li>1</li><li>2</li></ul>"))
    (is (= (html [:div (if true
                         [:span "foo"]
                         [:span "bar"])])
           "<div><span>foo</span></div>")))
  (testing "values are evaluated only once"
    (declare foo)
    (expect [foo (times 1 (returns "foo"))]
      (html [:div (foo)]))))

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

(deftest defhtml-macro
  (testing "basic html function"
    (defhtml basic-fn [x] [:span x])
    (is (= (basic-fn "foo") "<span>foo</span>")))
  (testing "html function with overloads"
    (defhtml overloaded-fn
      ([x] [:span x])
      ([x y] [:span x [:div y]]))
    (is (= (overloaded-fn "foo") "<span>foo</span>"))
    (is (= (overloaded-fn "foo" "bar")
           "<span>foo<div>bar</div></span>"))))

(deftest defelem-macro
  (testing "one overload function"
    (defelem one-form-two-args [a b] [b a 3])
    (is (thrown? IllegalArgumentException (one-form-two-args)))
    (is (thrown? IllegalArgumentException (one-form-two-args {})))
    (is (thrown? IllegalArgumentException (one-form-two-args 1)))
    (is (= [1 0 3] (one-form-two-args 0 1)))
    (is (= [1 {:foo :bar} 0 3] (one-form-two-args {:foo :bar} 0 1)))
    (is (thrown? IllegalArgumentException (one-form-two-args 1 2 3)))
    (is (thrown? IllegalArgumentException (one-form-two-args 1 2 3 4))))
  (testing "three overloads function"
    (defelem three-forms
      ([] [0])
      ([a] [(* a a) 2])
      ([a b] [b a]))
    (is (= [0] (three-forms)))
    (is (= [0 {:foo :bar}] (three-forms {:foo :bar})))
    (is (= [4 2] (three-forms 2)))
    (is (= [4 {:foo :bar} 2] (three-forms {:foo :bar} 2)))
    (is (= [1 0] (three-forms 0 1)))
    (is (= [1 {:foo :bar} 0] (three-forms {:foo :bar} 0 1)))
    (is (thrown? IllegalArgumentException (three-forms 1 2 3)))
    (is (thrown? IllegalArgumentException (three-forms 1 2 3 4))))
  (testing "recursive function"
    (defelem recursive [a]
      (if (< a 1) [a (inc a)] (recursive (- a 1))))
    (is (= [0 1] (recursive 4)))
    (is (= [0 {:foo :bar} 1] (recursive {:foo :bar} 4))))
  (testing "merge map if result has one"
    (defelem with-map
      ([] [1 {:foo :bar} 2])
      ([a b] [a {:foo :bar} b]))
    (is (= [1 {:foo :bar} 2] (with-map)))
    (is (= [1 {:a :b :foo :bar} 2] (with-map {:a :b})))
    (is (= [1 {:foo :bar} 2] (with-map 1 2)))
    (is (= [1 {:foo :bar :a :b} 2] (with-map {:a :b} 1 2))))
  (testing "preserve meta info"
    (defelem three-forms-extra
      "my documentation"
      {:my :attr}
      ([] {:pre [false]} [0])
      ([a] {:pre [(> a 1)]} [1])
      ([a b] {:pre [(> a 1)]} [1 2]))
    (is (thrown? AssertionError (three-forms-extra)))
    (is (thrown? AssertionError (three-forms-extra 0)))
    (is (thrown? AssertionError (three-forms-extra 0 0)))
    (is (= "my documentation" (:doc (meta #'three-forms-extra))))
    (is (= :attr (:my (meta #'three-forms-extra))))))
