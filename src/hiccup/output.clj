(ns hiccup.output
  "Renders nested maps representing a HTML DOM into a string.")

(defn render [node]
  (print (str "<" (:tag node)))
  (when-not (empty? (:attrs node))
    (print " ")
    (doseq [[k v] (:attrs node)]
      (print (str (name k) "=\"" v "\""))))
  (print ">")
  (doseq [content (:content node)]
    (if (map? content)
      (render content)
      (print content)))
  (print (str "</" (:tag node) ">")))
