(defproject hiccup/hiccup-compiler "1.0.4"
  :description "Hiccup data structure compilation functions"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [hiccup/hiccup-common "1.0.4"]]
  :aliases {"test-all"
            ["with-profile" "dev:1.3:1.4:1.5" "test"]}
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}})
