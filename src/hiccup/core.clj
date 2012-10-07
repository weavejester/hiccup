(ns hiccup.core
  (:require [clojure.string :as str])
  (:use [clojure.core.match :only (match)]
        [clojure.walk :only (postwalk)]))

(defn html
  ([])
  ([node & nodes]
     (mapcat html (cons node nodes)))
  ([node]
     (match [node]
       [[tag (attrs :guard map?) & content]]
       (list {:tag (name tag) :attrs attrs :content (apply html content)})
       [[tag & content]]
       (list {:tag (name tag) :attrs {} :content (apply html content)})
       [(content :guard seq?)]
       (apply html content)
       [other]
       (list (str other)))))

(defn css-sugar [nodes]
  (postwalk
   (fn [node]
     (if-let [tag (:tag node)]
       (let [base-tag    (re-find #"^[^#.]*" tag)
             tag-id      (second (re-find #"#([^#.]+)" tag))
             tag-classes (->> (re-seq #"\.([^#.]+)" tag)
                              (map second)
                              (str/join " "))]
         (-> node
             (assoc :tag base-tag)
             (assoc-in [:attrs :id] tag-id)
             (update-in [:attrs :class]
               (fn [class]
                 (cond
                  (str/blank? tag-classes) class       
                  (str/blank? class) tag-classes
                  :else (str class " " tag-classes))))))
       node))
   nodes))
