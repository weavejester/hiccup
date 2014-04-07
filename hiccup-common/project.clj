(defproject hiccup/hiccup-common "1.0.5"
  :description "Hiccup tag generation functions"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[org.clojure/clojure "1.2.1"]]
  :aliases {"test-all"
            ["with-profile" "test:1.3:1.4:1.5:1.6:cljs" "test"]}
  :plugins [[com.keminglabs/cljx "0.3.2"]]
  :hooks [cljx.hooks]
  :cljx {:builds [{:source-paths ["src"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["test"]
                   :output-path "target/test-classes"
                   :rules :clj}
                  {:source-paths ["src"]
                   :output-path "target/classes"
                   :rules :cljs}
                  {:source-paths ["test"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}
  :source-paths ["src" "target/classes"]
  :test-paths ["test" "target/test-classes"]
  :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :cljs {:dependencies [[org.clojure/clojure "1.6.0"]
                         [org.clojure/clojurescript "0.0-2202"]]
          :plugins [[com.cemerick/clojurescript.test "0.3.0"]
                    [lein-cljsbuild "1.0.3"]]
          :hooks [leiningen.cljsbuild]
          :cljsbuild {:builds [{:id "test"
                                :source-paths ["test" "target/classes"
                                               "target/test-classes"]
                                :compiler {:output-to "target/test/hiccup-common.js"
                                           :output-dir "target/test"
                                           :optimizations :whitespace
                                           :pretty-print true}}]
                      :test-commands {"phantom"
                                      ["phantomjs"
                                       :runner "target/test/hiccup-common.js"]}}}})
