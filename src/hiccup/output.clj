(ns hiccup.output
  "Renders nested maps representing a HTML DOM into a string."
  (:use hiccup.util))

(defn render [node]
  (print (as-str "<" (:tag node)))
  (when-not (empty? (:attrs node))
    (print " ")
    (doseq [[k v] (:attrs node)]
      (print (as-str k "=\"" (escape-html v) "\""))))
  (print ">")
  (doseq [content (:content node)]
    (if (map? content)
      (render content)
      (print (escape-html content))))
  (print (as-str "</" (:tag node) ">")))
