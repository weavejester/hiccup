;; Copyright (c) James Reeves. All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns hiccup.form-helpers
  "Functions for generating HTML forms and input fields."
  (:use
     clojure.contrib.java-utils
     [hiccup.core :only (defelem)]))

(defn- input-field
  "Creates a new <input> element."
  [type name value]
  [:input {:type type, :name name, :value value, :id name}])

(defelem hidden-field
  "Creates a hidden input field."
  ([name] (hidden-field name nil))
  ([name value] (input-field "hidden" name value)))

(defelem text-field
  "Creates a new text input field."
  ([name] (text-field name nil))
  ([name value] (input-field "text" name value)))

(defelem password-field
  "Creates a new password field."
  ([name] (password-field name nil))
  ([name value] (input-field "password" name value)))

(defelem check-box
  "Creates a check box."
  ([name] (check-box name nil))
  ([name checked?] (check-box name checked? "true"))
  ([name checked? value]
    [:input {:type "checkbox"
             :name name
             :id   name
             :value value
             :checked checked?}]))

(defelem radio-button
  "Creates a radio button."
  ([group] (radio-button group nil))
  ([group checked?] (radio-button group checked? "true"))
  ([group checked? value]
    [:input {:type "radio"
             :name group
             :id   (str (as-str group) "-" (as-str value))
             :value value
             :checked checked?}]))

(defelem select-options
  "Creates a seq of option tags from a collection."
  ([coll] (select-options coll nil))
  ([coll selected]
    (for [x coll]
      (if (sequential? x)
        (let [[text val] x]
          [:option {:value val :selected (= val selected)} text])
        [:option {:selected (= x selected)} x]))))

(defelem drop-down
  "Creates a drop-down box using the <select> tag."
  ([name options] (drop-down name options nil))
  ([name options selected]
    [:select {:name name :id name}
      (select-options options selected)]))

(defelem text-area
  "Creates a text area element."
  ([name] (text-area name nil))
  ([name value] [:textarea {:name name, :id name} value]))

(defelem file-upload
  "Creates a file upload input."
  [name]
  (input-field "file" name nil))

(defelem label
  "Creates a label for an input field with the supplied name."
  [name text]
  [:label {:for name} text])

(defelem submit-button
  "Creates a submit button."
  [text]
  [:input {:type "submit" :value text}])

(defelem reset-button
  "Creates a form reset button."
  [text]
  [:input {:type "reset" :value text}])

(defelem form-to
  "Create a form that points to a particular method and route.
  e.g. (form-to [:put \"/post\"]
         ...)"
  [[method action] & body]
  (let [method-str (.toUpperCase (name method))]
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action}]
          [:form {:method "POST", :action action}
            (hidden-field "_method" method-str)])
        (concat body)
        (vec))))
