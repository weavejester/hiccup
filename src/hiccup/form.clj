(ns hiccup.form
  "Functions for generating HTML forms and input fields."
  (:use hiccup.def
        hiccup.util))

(def ^:dynamic *group* [])

(defmacro with-group
  "Group together a set of related form fields for use with the Ring
  nested-params middleware."
  [group & body]
  `(binding [*group* (conj *group* (as-str ~group))]
     (list ~@body)))

(defn- make-name
  "Create a field name from the supplied argument the current field group."
  [name]
  (reduce #(str %1 "[" %2 "]")
          (conj *group* (as-str name))))

(defn- make-id
  "Create a field id from the supplied argument and current field group."
  [name]
  (reduce #(str %1 "-" %2)
          (conj *group* (as-str name))))

(defn- input-field
  "Creates a new <input> element."
  [type name value]
  [:input {:type  type
           :name  (make-name name)
           :id    (make-id name)
           :value value}])

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

(defelem email-field
  "Creates a new email input field."
  ([name] (email-field name nil))
  ([name value] (input-field "email" name value)))

(defelem check-box
  "Creates a check box."
  ([name] (check-box name nil))
  ([name checked?] (check-box name checked? "true"))
  ([name checked? value]
    [:input {:type "checkbox"
             :name (make-name name)
             :id   (make-id name)
             :value value
             :checked checked?}]))

(defelem radio-button
  "Creates a radio button."
  ([group] (radio-button group nil))
  ([group checked?] (radio-button group checked? "true"))
  ([group checked? value]
    [:input {:type "radio"
             :name (make-name group)
             :id   (make-id (str (as-str group) "-" (as-str value)))
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
    [:select {:name (make-name name), :id (make-id name)}
      (select-options options selected)]))

(defelem text-area
  "Creates a text area element."
  ([name] (text-area name nil))
  ([name value]
    [:textarea {:name (make-name name), :id (make-id name)}
      (escape-html value)]))

(defelem file-upload
  "Creates a file upload input."
  [name]
  (input-field "file" name nil))

(defelem label
  "Creates a label for an input field with the supplied name."
  [name text]
  [:label {:for (make-id name)} text])

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
  (let [method-str (.toUpperCase (name method))
        action-uri (to-uri action)]
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action-uri}]
          [:form {:method "POST", :action action-uri}
            (hidden-field "_method" method-str)])
        (concat body)
        (vec))))
