(ns hiccup.core
  (:require [clojure.string :as str])
  (:use [clojure.core.match :only (match)]
        [clojure.walk :only (postwalk)]))

(defn xml
  "Parse a tree of vectors into a clojure.xml-compatible data structure.
  The vectors must be in the format:
    [tag attr-map & content]
  Or:
    [tag & content]"
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
       (list other))))

(defn css-sugar
  "Walk a clojure.xml tree and parse any tags with CSS-style IDs or classes in
  their names.

  For example:
    {:tag \"foo#bar.baz\" :attrs {}}
    => {:tag \"foo\" :attrs {:id \"bar\" :class \"baz\"}}"
  [nodes]
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

(defn html
  "Parse a tree of vectors into a clojure.xml-compatible data structure.
  Equivalent to the composition of the xml and css-sugar functions."
  [nodes]
  (css-sugar (xml nodes)))
