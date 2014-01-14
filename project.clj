(defproject hiccup "1.0.4"
  :description "A fast library for rendering HTML in Clojure"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[hiccup/hiccup-common "1.0.4"]
                 [hiccup/hiccup-compiler "1.0.4"]
                 [org.clojure/clojure "1.2.1"]]
  :plugins [[codox "0.6.4"]
            [lein-sub "0.2.4"]]
  :codox {:exclude [hiccup.compiler]
          :sources ["hiccup-common/src"
                    "hiccup-compiler/src"]
          :src-dir-uri "http://github.com/weavejester/hiccup/blob/1.0.4"
          :src-linenum-anchor-prefix "L"}
  :sub ["hiccup-common"
        "hiccup-compiler"]
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}})
