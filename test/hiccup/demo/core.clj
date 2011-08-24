(ns hiccup.demo.core
  (:use [hiccup core form-helpers page-helpers jquery]
        [ring.middleware file])
  (:require [ring.util.serve :as serve]))

(defn section
  "Define a section of the demo."
  [title & body]
  [:div.section
   [:h1 title]
   body])

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
     (section "Headers"
      (for [x (range 6)]
        [(str "h" (inc x)) "Header " (inc x)]))
     
     (section "jQuery"
              (make-sortable
               [:ul
                (for [x (range 5)]
                  [:li "Sortable Item " x [:ul [:li "Subitem 1"] [:li "Subitem 2"]]])]))

     (section "jQuery Forms"
              (rank-options :rankopts {:dog "Puppy" :cat "Kitten" :horse "Colt"}))])})

(def wapp (wrap-file app "resources"))

(defn run [] (serve/serve wapp 8080))
