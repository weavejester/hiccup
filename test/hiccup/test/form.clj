(ns hiccup.test.form
  (:use clojure.test
        hiccup.core
        hiccup.form))

(deftest test-hidden-field
  (is (= (html (hidden-field :foo "bar"))
         "<input id=\"foo\" name=\"foo\" type=\"hidden\" value=\"bar\" />")))

(deftest test-hidden-field-with-extra-atts
  (is (= (html (hidden-field {:class "classy"} :foo "bar"))
         "<input class=\"classy\" id=\"foo\" name=\"foo\" type=\"hidden\" value=\"bar\" />")))

(deftest test-text-field
  (is (= (html (text-field :foo))
         "<input id=\"foo\" name=\"foo\" type=\"text\" />")))

(deftest test-text-field-with-extra-atts
  (is (= (html (text-field {:class "classy"} :foo "bar"))
         "<input class=\"classy\" id=\"foo\" name=\"foo\" type=\"text\" value=\"bar\" />")))

(deftest test-check-box
  (is (= (html (check-box :foo true))
         (str "<input checked=\"checked\" id=\"foo\" name=\"foo\""
              " type=\"checkbox\" value=\"true\" />"))))

(deftest test-check-box-with-extra-atts
  (is (= (html (check-box {:class "classy"} :foo true 1))
         (str "<input checked=\"checked\" class=\"classy\" id=\"foo\" name=\"foo\""
              " type=\"checkbox\" value=\"1\" />"))))

(deftest test-password-field
  (is (= (html (password-field :foo "bar"))
         "<input id=\"foo\" name=\"foo\" type=\"password\" value=\"bar\" />")))

(deftest test-password-field-with-extra-atts
  (is (= (html (password-field {:class "classy"} :foo "bar"))
         "<input class=\"classy\" id=\"foo\" name=\"foo\" type=\"password\" value=\"bar\" />")))

(deftest test-email-field
  (is (= (html (email-field :foo "bar"))
         "<input id=\"foo\" name=\"foo\" type=\"email\" value=\"bar\" />")))

(deftest test-email-field-with-extra-atts
  (is (= (html (email-field {:class "classy"} :foo "bar"))
         "<input class=\"classy\" id=\"foo\" name=\"foo\" type=\"email\" value=\"bar\" />")))

(deftest test-date-field
  (is (= (html (date-field :foo "2001-05-11"))
         "<input id=\"foo\" name=\"foo\" type=\"date\" value=\"2001-05-11\" />")))

(deftest test-date-field-with-extra-atts
  (is (= (html (date-field {:class "classy" :min "1991-03-12" :max "2012-12-21"} :foo "2001-05-11"))
         (str "<input class=\"classy\" id=\"foo\" max=\"2012-12-21\" min=\"1991-03-12\" "
              "name=\"foo\" type=\"date\" value=\"2001-05-11\" />"))))

(deftest test-datetime-field
  (is (= (html (datetime-field :foo "1990-12-31T23:59:60Z"))
         "<input id=\"foo\" name=\"foo\" type=\"datetime\" value=\"1990-12-31T23:59:60Z\" />")))

(deftest test-datetime-field-with-extra-atts
  (is (= (html (datetime-field {:class "classy" :min "1990-12-31T23:59:60Z" :max "1996-12-19T16:39:57-08:00"}
                               :foo "1991-12-31T23:59:60Z"))
         (str "<input class=\"classy\" id=\"foo\" max=\"1996-12-19T16:39:57-08:00\" "
              "min=\"1990-12-31T23:59:60Z\" name=\"foo\" type=\"datetime\" "
              "value=\"1991-12-31T23:59:60Z\" />"))))

(deftest test-datetime-local-field
  (is (= (html (datetime-local-field :foo "1985-04-12T23:20:50.52"))
         "<input id=\"foo\" name=\"foo\" type=\"datetime-local\" value=\"1985-04-12T23:20:50.52\" />")))

(deftest test-datetime-local-field-with-extra-atts
  (is (= (html (datetime-local-field {:class "classy" :min "1985-04-12T23:20:50.52" :max "1996-12-19T16:39:57"}
                                     :foo "1990-04-12T23:20:50.52"))
         (str "<input class=\"classy\" id=\"foo\" max=\"1996-12-19T16:39:57\" min=\"1985-04-12T23:20:50.52\" "
              "name=\"foo\" type=\"datetime-local\" value=\"1990-04-12T23:20:50.52\" />"))))

(deftest test-month-field
  (is (= (html (month-field :foo "1996-12"))
         "<input id=\"foo\" name=\"foo\" type=\"month\" value=\"1996-12\" />")))

(deftest test-month-field-with-extra-atts
  (is (= (html (month-field {:class "classy" :min "1991-03" :max "2012-05"} :foo "1996-12"))
         (str "<input class=\"classy\" id=\"foo\" max=\"2012-05\" min=\"1991-03\" "
              "name=\"foo\" type=\"month\" value=\"1996-12\" />"))))

(deftest test-week-field
  (is (= (html (week-field :foo "1996-W16"))
         "<input id=\"foo\" name=\"foo\" type=\"week\" value=\"1996-W16\" />")))

(deftest test-week-field-with-extra-atts
  (is (= (html (week-field {:class "classy" :min "1991-W05" :max "2012-W22"} :foo "1996-W16"))
         (str "<input class=\"classy\" id=\"foo\" max=\"2012-W22\" min=\"1991-W05\" "
              "name=\"foo\" type=\"week\" value=\"1996-W16\" />"))))

(deftest test-time-field
  (is (= (html (time-field :foo "17:39:57"))
         "<input id=\"foo\" name=\"foo\" type=\"time\" value=\"17:39:57\" />")))

(deftest test-time-field-with-extra-atts
  (is (= (html (time-field {:class "classy" :min "12:39:57" :max "23:20:50.52"} :foo "17:39:57"))
         (str "<input class=\"classy\" id=\"foo\" max=\"23:20:50.52\" min=\"12:39:57\" "
              "name=\"foo\" type=\"time\" value=\"17:39:57\" />"))))

(deftest test-color-field
  (is (= (html (color-field :foo "#ff0000"))
         "<input id=\"foo\" name=\"foo\" type=\"color\" value=\"#ff0000\" />")))

(deftest test-color-field-with-extra-atts
  (is (= (html (color-field {:class "classy"} :foo "#ff0000"))
         "<input class=\"classy\" id=\"foo\" name=\"foo\" type=\"color\" value=\"#ff0000\" />")))

(deftest test-number-field
  (is (= (html (number-field :foo "5"))
         "<input id=\"foo\" name=\"foo\" type=\"number\" value=\"5\" />")))

(deftest test-number-field-with-extra-atts
  (is (= (html (number-field {:class "classy" :min "1" :max "10"} :foo "5"))
         (str "<input class=\"classy\" id=\"foo\" max=\"10\" min=\"1\" "
              "name=\"foo\" type=\"number\" value=\"5\" />"))))

(deftest test-range-field
  (is (= (html (range-field :foo "5"))
         "<input id=\"foo\" name=\"foo\" type=\"range\" value=\"5\" />")))

(deftest test-range-field-with-extra-atts
  (is (= (html (range-field {:class "classy" :min "1" :max "10"} :foo "5"))
         (str "<input class=\"classy\" id=\"foo\" max=\"10\" min=\"1\" "
              "name=\"foo\" type=\"range\" value=\"5\" />"))))

(deftest test-url-field
  (is (= (html (url-field :foo "http://clojure.org"))
         "<input id=\"foo\" name=\"foo\" type=\"url\" value=\"http://clojure.org\" />")))

(deftest test-url-field-with-extra-atts
  (is (= (html (url-field {:class "classy"} :foo "http://clojure.org"))
         (str "<input class=\"classy\" id=\"foo\" name=\"foo\" "
              "type=\"url\" value=\"http://clojure.org\" />"))))

(deftest test-search-field
  (is (= (html (search-field :foo "bar"))
         "<input id=\"foo\" name=\"foo\" type=\"search\" value=\"bar\" />")))

(deftest test-search-field-with-extra-atts
  (is (= (html (search-field {:class "classy" :placeholder "baz"} :foo "bar"))
         (str "<input class=\"classy\" id=\"foo\" name=\"foo\" placeholder=\"baz\" "
              "type=\"search\" value=\"bar\" />"))))

(deftest test-tel-field
  (is (= (html (tel-field :foo "bar"))
         "<input id=\"foo\" name=\"foo\" type=\"tel\" value=\"bar\" />")))

(deftest test-tel-field-with-extra-atts
  (is (= (html (tel-field {:class "classy"} :foo "bar"))
         "<input class=\"classy\" id=\"foo\" name=\"foo\" type=\"tel\" value=\"bar\" />")))

(deftest test-radio-button
  (is (= (html (radio-button :foo true 1))
         (str "<input checked=\"checked\" id=\"foo-1\" name=\"foo\""
              " type=\"radio\" value=\"1\" />"))))

(deftest test-radio-button-with-extra-atts
  (is (= (html (radio-button {:class "classy"} :foo true 1))
         (str "<input checked=\"checked\" class=\"classy\" id=\"foo-1\" name=\"foo\""
              " type=\"radio\" value=\"1\" />"))))

(deftest test-select-options
  (are [x y] (= (html x) y)
    (select-options ["foo" "bar" "baz"])
      "<option>foo</option><option>bar</option><option>baz</option>"
    (select-options ["foo" "bar"] "bar")
      "<option>foo</option><option selected=\"selected\">bar</option>"
    (select-options [["Foo" 1] ["Bar" 2]])
      "<option value=\"1\">Foo</option><option value=\"2\">Bar</option>"))

(deftest test-drop-down
  (let [options ["op1" "op2"]
        selected "op1"
        select-options (html (select-options options selected))]
    (is (= (html (drop-down :foo options selected))
           (str "<select id=\"foo\" name=\"foo\">" select-options "</select>")))))

(deftest test-drop-down-with-extra-atts
  (let [options ["op1" "op2"]
        selected "op1"
        select-options (html (select-options options selected))]
    (is (= (html (drop-down {:class "classy"} :foo options selected))
           (str "<select class=\"classy\" id=\"foo\" name=\"foo\">"
                select-options "</select>")))))

(deftest test-text-area
  (is (= (html (text-area :foo "bar"))
         "<textarea id=\"foo\" name=\"foo\">bar</textarea>")))

(deftest test-text-area-field-with-extra-atts
  (is (= (html (text-area {:class "classy"} :foo "bar"))
         "<textarea class=\"classy\" id=\"foo\" name=\"foo\">bar</textarea>")))

(deftest test-text-area-escapes
  (is (= (html (text-area :foo "bar</textarea>"))
         "<textarea id=\"foo\" name=\"foo\">bar&lt;/textarea&gt;</textarea>")))

(deftest test-file-field
  (is (= (html (file-upload :foo))
         "<input id=\"foo\" name=\"foo\" type=\"file\" />")))

(deftest test-file-field-with-extra-atts
  (is (= (html (file-upload {:class "classy"} :foo))
         (str "<input class=\"classy\" id=\"foo\" name=\"foo\""
              " type=\"file\" />"))))

(deftest test-label
  (is (= (html (label :foo "bar"))
         "<label for=\"foo\">bar</label>")))

(deftest test-label-with-extra-atts
  (is (= (html (label {:class "classy"} :foo "bar"))
         "<label class=\"classy\" for=\"foo\">bar</label>")))

(deftest test-submit
  (is (= (html (submit-button "bar"))
         "<input type=\"submit\" value=\"bar\" />")))

(deftest test-submit-button-with-extra-atts
  (is (= (html (submit-button {:class "classy"} "bar"))
         "<input class=\"classy\" type=\"submit\" value=\"bar\" />")))

(deftest test-reset-button
  (is (= (html (reset-button "bar"))
         "<input type=\"reset\" value=\"bar\" />")))

(deftest test-reset-button-with-extra-atts
  (is (= (html (reset-button {:class "classy"} "bar"))
         "<input class=\"classy\" type=\"reset\" value=\"bar\" />")))

(deftest test-form-to
  (is (= (html (form-to [:post "/path"] "foo" "bar"))
         "<form action=\"/path\" method=\"POST\">foobar</form>")))

(deftest test-form-to-with-hidden-method
  (is (= (html (form-to [:put "/path"] "foo" "bar"))
         (str "<form action=\"/path\" method=\"POST\">"
              "<input id=\"_method\" name=\"_method\" type=\"hidden\" value=\"PUT\" />"
              "foobar</form>"))))

(deftest test-form-to-with-extr-atts
  (is (= (html (form-to {:class "classy"} [:post "/path"] "foo" "bar"))
         "<form action=\"/path\" class=\"classy\" method=\"POST\">foobar</form>")))

(deftest test-with-group
  (testing "hidden-field"
    (is (= (html (with-group :foo (hidden-field :bar "val")))
           "<input id=\"foo-bar\" name=\"foo[bar]\" type=\"hidden\" value=\"val\" />")))
  (testing "text-field"
    (is (= (html (with-group :foo (text-field :bar)))
           "<input id=\"foo-bar\" name=\"foo[bar]\" type=\"text\" />")))
  (testing "checkbox"
    (is (= (html (with-group :foo (check-box :bar)))
           "<input id=\"foo-bar\" name=\"foo[bar]\" type=\"checkbox\" value=\"true\" />")))
  (testing "password-field"
    (is (= (html (with-group :foo (password-field :bar)))
           "<input id=\"foo-bar\" name=\"foo[bar]\" type=\"password\" />")))
  (testing "radio-button"
    (is (= (html (with-group :foo (radio-button :bar false "val")))
           "<input id=\"foo-bar-val\" name=\"foo[bar]\" type=\"radio\" value=\"val\" />")))
  (testing "drop-down"
    (is (= (html (with-group :foo (drop-down :bar [])))
           (str "<select id=\"foo-bar\" name=\"foo[bar]\"></select>"))))
  (testing "text-area"
    (is (= (html (with-group :foo (text-area :bar)))
           (str "<textarea id=\"foo-bar\" name=\"foo[bar]\"></textarea>"))))
  (testing "file-upload"
    (is (= (html (with-group :foo (file-upload :bar)))
           "<input id=\"foo-bar\" name=\"foo[bar]\" type=\"file\" />")))
  (testing "label"
    (is (= (html (with-group :foo (label :bar "Bar")))
           "<label for=\"foo-bar\">Bar</label>")))
  (testing "multiple with-groups"
    (is (= (html (with-group :foo (with-group :bar (text-field :baz))))
           "<input id=\"foo-bar-baz\" name=\"foo[bar][baz]\" type=\"text\" />")))
  (testing "multiple elements"
    (is (= (html (with-group :foo (label :bar "Bar") (text-field :var)))
           "<label for=\"foo-bar\">Bar</label><input id=\"foo-var\" name=\"foo[var]\" type=\"text\" />"))))
