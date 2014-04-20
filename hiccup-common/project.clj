(defproject hiccup/hiccup-common "2.0.0-SNAPSHOT"
  :description "Library for generating hiccup data structures"
  :url "http://github.com/hiccup/hiccup-tags"
  :dependencies [[org.clojure/clojure "1.2.1"]]
  :plugins [[codox "0.6.6"]]
  :codox {:src-dir-uri "http://github.com/weavejester/hiccup/blob/2.0.0/"
          :src-linenum-anchor-prefix "L"}
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :aliases {"test-all"
            ["with-profile" "dev:1.3:1.4:1.5:1.6" "test"]})
