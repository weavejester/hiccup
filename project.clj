(defproject hiccup "1.0.5"
  :description "A fast library for rendering HTML in Clojure"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[org.clojure/clojure "1.2.1"]]
  :plugins [[codox "0.7.4"]]
  :codox {:exclude [hiccup.compiler]
          :sources ["src"]
          :src-dir-uri "http://github.com/weavejester/hiccup/blob/1.0.5/"
          :src-linenum-anchor-prefix "L"}

  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :dev {:plugins [[midje-readme "1.0.2"]
                   [lein-midje "3.0.0"]]
         :dependencies [[midje "1.5.0"]
                        [org.clojure/clojure "1.5.1"]]}

   })
