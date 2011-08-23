(ns hiccup.jquery
  (:use hiccup.core))

(defelem jquery-link
  "Generate a link to the Google jQuery CDN"
  ([] (jquery-link "1.4.2"))
  ([version] [:script {:src (str "http://ajax.googleapis.com/ajax/libs/jquery/" version "/jquery.min.js")}]))

(defelem jquery-ui-link
  "Generate a link to the Google jQuery UI CDN"
  ([] (jquery-ui-link "1.8.1"))
  ([version] [:script {:src (str "http://ajax.googleapis.com/ajax/libs/jqueryui/" version "/jquery-ui.min.js")}]))

;             "selectToUISlider.jQuery.js"
;             "local.js"

