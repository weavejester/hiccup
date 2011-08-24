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
     (section "Standard Elements"
      (for [x (range 6)]
        [(str "h" (inc x)) "Header " (inc x)]))

     (section "Form Helpers"
              [:div "Make a choice: "
               (radio-group :test-radio-group
                            {:one "First choice"
                             :two "Second Choice"
                             :three "Third Choice"})]
              [:div "Do you like ice cream?"
               (yes-no :test-yes-no "yes")]
              [:div "Check the editors you have used:"
               (with-group :editors
                 (doall
                  (for [[name lbl] {:emacs "Emacs" :vim "VIM" :eclipse "Eclipse"}]
                    (labeled-checkbox name lbl))))])
     
     (section "jQuery"
              (make-sortable
               [:ul
                (for [x (range 5)]
                  [:li "Sortable Item " x [:ul [:li "Subitem 1"] [:li "Subitem 2"]]])]))

     (section "jQuery Forms"
              (rank-options :rankopts {:dog "Puppy" :cat "Kitten" :horse "Colt"}))])})

(def wapp (wrap-file app "resources"))

(defn run [] (serve/serve wapp 8080))
