(ns hiccup.demo.core
  (:use [hiccup core form-helpers page-helpers jquery]
        [ring.middleware file])
  (:require [ring.util.serve :as serve]))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (xhtml
    [:head [:title "Hiccup Demo"]
     (jquery-link)
     (jquery-ui-link)
     (include-js "hiccup.jquery.support.js" "hiccup.jqueryui.support.js")]
    [:body
     [:div#headers
      (for [x (range 6)]
        [(str "h" (inc x)) "Header " (inc x)])]
     [:p "This is a paragraph."]
     (make-sortable [:ul (for [x (range 5)] [:li "Sortable Item " x [:ul [:li "Subitem 1"] [:li "Subitem 2"]]])])])})

(def wapp (wrap-file app "resources"))

(defn run [] (serve/serve wapp 8080))
