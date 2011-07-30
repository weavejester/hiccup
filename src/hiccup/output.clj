(ns hiccup.output
  "Renders nested maps representing a HTML DOM into a string."
  (:use hiccup.util))

(defn to-string [node]
  (with-out-str
    (print (str "<" (as-str (:tag node))))
    (when-not (empty? (:attrs node))
      (print " ")
      (doseq [[k v] (:attrs node)]
        (print (str (as-str k) "=\"" (escape-html v) "\""))))
    (print ">")
    (doseq [content (:content node)]
      (if (map? content)
        (render content)
        (print (escape-html content))))
    (print (str "</" (as-str (:tag node)) ">"))))
