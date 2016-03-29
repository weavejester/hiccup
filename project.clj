(defproject hiccup "1.0.5"
  :description "A fast library for rendering HTML in Clojure"
  :url "http://github.com/weavejester/hiccup"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.2.1"]]
  :plugins [[codox "0.7.4"]]
  :codox {:exclude [hiccup.compiler]
          :sources ["src"]
          :src-dir-uri "http://github.com/weavejester/hiccup/blob/1.0.5/"
          :src-linenum-anchor-prefix "L"}
  :aliases {"test-all" ["with-profile" "default:+1.3:+1.4:+1.5" "test"]}
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}})
