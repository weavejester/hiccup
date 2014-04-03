(ns hiccup.test.form
  #+clj (:use clojure.test
              hiccup.form
              hiccup.util)
  #+cljs (:require [cemerick.cljs.test :refer-macros [deftest is testing]]
                   [hiccup.form :refer [check-box drop-down email-field file-upload
                                        form-field form-to hidden-field label
                                        password-field radio-button reset-button
                                        select-options submit-button text-area
                                        text-field]]
                   [hiccup.util :refer [to-uri]])
  #+cljs (:require-macros [hiccup.form :refer [with-group]]))

(deftest test-hidden-field
  (is (= (hidden-field :foo "bar")
         [:input {:id "foo" :name "foo" :type "hidden" :value "bar"}])))

(deftest test-hidden-field-with-extra-atts
  (is (= (hidden-field {:class "classy"} :foo "bar")
         [:input  {:class "classy" :id "foo" :name "foo" :type "hidden" :value "bar"}])))

(deftest test-text-field
  (is (= (text-field :foo)
         [:input {:id "foo" :name "foo" :type "text" :value nil}])))

(deftest test-text-field-with-extra-atts
  (is (= (text-field {:class "classy"} :foo "bar")
         [:input {:class "classy" :id "foo" :name "foo" :type "text" :value "bar"}])))

(deftest test-check-box
  (is (= (check-box :foo true)
         [:input {:checked true :id "foo" :name "foo" :type "checkbox" :value "true"}])))

(deftest test-check-box-with-extra-atts
  (is (= (check-box {:class "classy"} :foo true 1)
         [:input {:checked true :class "classy" :id "foo" :name "foo" :type "checkbox" :value 1}])))

(deftest test-password-field
  (is (= (password-field :foo "bar")
         [:input {:id "foo" :name "foo" :type "password" :value "bar"}])))

(deftest test-password-field-with-extra-atts
  (is (= (password-field {:class "classy"} :foo "bar")
         [:input {:class "classy" :id "foo" :name "foo" :type "password" :value "bar"}])))

(deftest test-email-field
  (is (= (email-field :foo "bar")
         [:input {:id "foo" :name "foo" :type "email" :value "bar"}])))

(deftest test-email-field-with-extra-atts
  (is (= (email-field {:class "classy"} :foo "bar")
         [:input {:class "classy" :id "foo" :name "foo" :type "email" :value "bar"}])))

(deftest test-radio-button
  (is (= (radio-button :foo true 1)
         [:input {:checked true :id "foo-1" :name "foo" :type "radio" :value 1}])))

(deftest test-radio-button-with-extra-atts
  (is (= (radio-button {:class "classy"} :foo true 1)
         [:input {:class "classy" :checked true :id "foo-1" :name "foo" :type "radio" :value 1}])))

(deftest test-select-options
  (is (= (select-options ["foo" "bar" "baz"])
         '([:option {:selected false} "foo"]
           [:option {:selected false} "bar"]
           [:option {:selected false} "baz"])))
  (is (= (select-options ["foo" "bar"] "bar")
         '([:option {:selected false} "foo"] [:option {:selected true} "bar"])))
  (is (= (select-options [["Foo" 1] ["Bar" 2]])
         '([:option {:value 1 :selected false} "Foo"]
           [:option {:value 2 :selected false} "Bar"])))
  (is (= (select-options [["Foo" [1 2]] ["Bar" [3 4]]])
         '([:optgroup {:label "Foo"} ([:option {:selected false} 1] [:option {:selected false} 2])]
           [:optgroup {:label "Bar"} ([:option {:selected false} 3] [:option {:selected false} 4])])))
  (is (= (select-options [["Foo" [["bar" 1] ["baz" 2]]]])
         '([:optgroup {:label "Foo"} ([:option {:value 1 :selected false} "bar"]
                                      [:option {:value 2 :selected false} "baz"])])))
  (is (= (select-options [["Foo" [1 2]]] 2)
         '([:optgroup {:label "Foo"} ([:option {:selected false} 1] [:option {:selected true} 2])]))))

(deftest test-drop-down
  (let [options ["op1" "op2"]
        selected "op1"
        select-options (select-options options selected)]
    (is (= (drop-down :foo options selected)
           [:select {:id "foo" :name "foo"} select-options]))))

(deftest test-drop-down-with-extra-atts
  (let [options ["op1" "op2"]
        selected "op1"
        select-options (select-options options selected)]
    (is (= (drop-down {:class "classy"} :foo options selected)
           [:select {:class "classy" :name "foo" :id "foo"}
            '([:option {:selected true} "op1"] [:option {:selected false} "op2"])]))))

(deftest test-text-area
  (is (= (text-area :foo "bar")
         [:textarea {:name "foo" :id "foo"} "bar"])))

(deftest test-text-area-field-with-extra-atts
  (is (= (text-area {:class "classy"} :foo "bar")
         [:textarea {:name "foo" :class "classy" :id "foo"} "bar"])))

(deftest test-text-area-escapes
  (is (= (text-area :foo "bar</textarea>")
         [:textarea {:name "foo" :id "foo"} "bar&lt;/textarea&gt;"])))

(deftest test-file-field
  (is (= (file-upload :foo)
         [:input {:type "file" :name "foo" :id "foo" :value nil}])))

(deftest test-file-field-with-extra-atts
  (is (= (file-upload {:class "classy"} :foo)
         [:input {:class "classy" :id "foo" :name "foo" :type "file" :value nil}])))

(deftest test-label
  (is (= (label :foo "bar")
         [:label {:for "foo"} "bar"])))

(deftest test-label-with-extra-atts
  (is (= (label {:class "classy"} :foo "bar")
         [:label {:class "classy" :for "foo"} "bar"])))

(deftest test-submit
  (is (= (submit-button "bar")
         [:input {:type "submit" :value "bar"}])))

(deftest test-submit-button-with-extra-atts
  (is (= (submit-button {:class "classy"} "bar")
         [:input {:class "classy" :type "submit" :value "bar"}])))

(deftest test-reset-button
  (is (= (reset-button "bar")
         [:input {:type "reset" :value "bar"}])))

(deftest test-reset-button-with-extra-atts
  (is (= (reset-button {:class "classy"} "bar")
         [:input {:class "classy" :type "reset" :value "bar"}])))

(deftest test-form-to
  (is (= (form-to [:post "/path"] "foobar")
         [:form {:action (to-uri "/path") :method "POST"} "foobar"])))

(deftest test-form-to-with-hidden-method
  (is (= (form-to [:put "/path"] "foo" "bar")
         [:form {:action (to-uri "/path") :method "POST"}
          [:input {:id "_method" :name "_method" :type "hidden" :value "PUT"}]
          "foo" "bar"])))

(deftest test-form-to-with-extr-atts
  (is (= (form-to {:class "classy"} [:post "/path"] "foo" "bar")
         [:form {:action (to-uri "/path") :method "POST" :class "classy"} "foo" "bar"])))

(deftest test-with-group
  (testing "hidden-field"
    (is (= (with-group :foo (hidden-field :bar "val"))
           '([:input {:type "hidden" :name "foo[bar]" :id "foo-bar" :value "val"}]))))
  (testing "text-field"
    (is (= (with-group :foo (text-field :bar))
           '([:input {:type "text" :name "foo[bar]" :id "foo-bar" :value nil}]))))
  (testing "checkbox"
    (is (= (with-group :foo (check-box :bar))
           '([:input {:type "checkbox" :name "foo[bar]" :id "foo-bar" :value "true" :checked nil}]))))
  (testing "password-field"
    (is (= (with-group :foo (password-field :bar))
           '([:input {:type "password" :name "foo[bar]" :id "foo-bar" :value nil}]))))
  (testing "radio-button"
    (is (= (with-group :foo (radio-button :bar false "val"))
           '([:input {:type "radio" :name "foo[bar]" :id "foo-bar-val" :value "val" :checked false}]))))
  (testing "drop-down"
    (is (= (with-group :foo (drop-down :bar []))
           '([:select {:name "foo[bar]" :id "foo-bar"} ()]))))
  (testing "text-area"
    (is (= (with-group :foo (text-area :bar))
           '([:textarea {:name "foo[bar]" :id "foo-bar"} ""]))))
  (testing "file-upload"
    (is (= (with-group :foo (file-upload :bar))
           '([:input {:type "file" :name "foo[bar]" :id "foo-bar" :value nil}]))))
  (testing "label"
    (is (= (with-group :foo (label :bar "Bar"))
           '([:label {:for "foo-bar"} "Bar"]))))
  (testing "multiple with-groups"
    (is (= (with-group :foo (with-group :bar (text-field :baz)))
           '(([:input {:type "text" :name "foo[bar][baz]" :id "foo-bar-baz" :value nil}])))))
  (testing "multiple elements"
    (is (= (with-group :foo (label :bar "Bar") (text-field :var))
           '([:label {:for "foo-bar"} "Bar"]
             [:input {:id "foo-var" :name "foo[var]" :type "text" :value nil}])))))
