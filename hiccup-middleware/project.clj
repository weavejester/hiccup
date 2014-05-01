(defproject hiccup/hiccup-middleware "2.0.0-SNAPSHOT"
  :description "Library containing Ring middleware for Hiccup."
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [hiccup/hiccup-common "2.0.0-SNAPSHOT"]]
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :aliases {"test-all"
            ["with-profile" "dev:1.3:1.4:1.5:1.6" "test"]})
