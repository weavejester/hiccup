(ns hiccup.jquery
  (:use [hiccup core form-helpers]))

(defelem jquery-link
  "Generate a link to the Google jQuery CDN"
  ([] (jquery-link "1.4.2"))
  ([version] [:script {:src (str "http://ajax.googleapis.com/ajax/libs/jquery/" version "/jquery.min.js")}]))

(defelem jquery-ui-link
  "Generate a link to the Google jQuery UI CDN"
  ([] (jquery-ui-link "1.8.1"))
  ([version] [:script {:src (str "http://ajax.googleapis.com/ajax/libs/jqueryui/" version "/jquery-ui.min.js")}]))

(defn make-sortable
  "Make the immediate children of a tag sortable within the tag
Requires jQuery UI and hiccup.jqueryui.support.js"
  [tag]
  (add-class tag :hiccup-jquery-sortable))

(defn rank-options
  "Given a map of {:id \"label\"}, let the user rank order the options. Degrades to a set of labeled boxes."
  [name options]
  (make-sortable
   (with-group name
     [:ul.hiccup-jquery-rank-options
      (doall ; eagerness so that with-group will work
       (map
        (fn [rank [id labeltxt]]
          [:li.hiccup-jquery-rank-item (text-field {:style "display: none;"} id rank) (label id labeltxt)])
        (range)
        options))])))

