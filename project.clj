(defproject hiccup "2.0.0-RC4"
  :description "A fast library for rendering HTML in Clojure"
  :url "http://github.com/weavejester/hiccup"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :plugins [[lein-codox "0.10.8"]]
  :codox
  {:output-path "codox"
   :source-uri  "http://github.com/weavejester/hiccup/blob/{version}/{filepath}#L{line}"
   :namespaces  [#"^hiccup2?\.(?!compiler)"]
   :metadata    {:doc/format :markdown}}
  :aliases {"test-all" ["with-profile" "default:+1.8:+1.9:+1.10:+1.11" "test"]}
  :profiles
  {:dev {:dependencies [[criterium "0.4.4"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
   :1.11 {:dependencies [[org.clojure/clojure "1.11.1"]]}}
  :global-vars {*warn-on-reflection* true})
