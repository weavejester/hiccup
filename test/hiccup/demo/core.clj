(ns hiccup.demo.core
  (:use [hiccup core form-helpers page-helpers jquery])
  (:require [ring.util.serve :as serve]))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (xhtml
    [:head [:title "Hiccup Demo"] (jquery-link) (jquery-ui-link)]
    [:body
     [:div#headers
      (for [x (range 6)]
        [(str "h" (inc x)) "Header " (inc x)])]])})

(defn run [] (serve/serve app 8080))
