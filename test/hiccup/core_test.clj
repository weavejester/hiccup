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
  (is (= (html [:div [:p]]) "<div><p /></div>"))
  (is (= (html [:div [:b]]) "<div><b></b></div>"))
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

(deftest attr-keys-can-be-strs
  (is (= (html [:img {"id" "foo"}]) "<img id=\"foo\" />")))

(deftest attr-key-can-be-symbols
  (is (= (html [:img {'id "foo"}]) "<img id=\"foo\" />")))

(deftest attr-keys-different-types
  (is (= (html [:xml {:a "1", 'b "2", "c" "3"}])
         "<xml a=\"1\" b=\"2\" c=\"3\" />")))

(deftest attrs-can-contain-vars
  (let [x "foo"]
    (is (= (html [:xml {:x x}]) "<xml x=\"foo\" />"))
    (is (= (html [:xml {x "x"}]) "<xml foo=\"x\" />"))
    (is (= (html [:xml {:x x} "bar"])
           "<xml x=\"foo\">bar</xml>"))))

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

(deftest content-can-be-forms
  (is (= (html [:span (str (+ 1 1))])
         "<span>2</span>"))
  (is (= (html [:span ({:foo "bar"} :foo)])
         "<span>bar</span>")))

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

(deftest mode-bool-attrs
  (is (= (html {:mode :xml} [:input {:type "checkbox" :checked true}])
         "<input checked=\"checked\" type=\"checkbox\" />"))
  (is (= (html {:mode :sgml} [:input {:type "checkbox" :checked true}])
         "<input checked type=\"checkbox\">")))

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

