(ns hiccup.core
  (:use [clojure.core.match :only (match)]))

(defn html
  ([])
  ([node & nodes]
     (mapcat html (cons node nodes)))
  ([node]
     (match [node]
       [[tag (attrs :guard map?) & content]]
       (list {:tag tag :attrs attrs :content (apply html content)})
       [[tag & content]]
       (list {:tag tag :attrs {} :content (apply html content)})
       [(content :guard seq?)]
       (apply html content)
       [other]
       (list (str other)))))
