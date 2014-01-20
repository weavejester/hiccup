(defproject hiccup/hiccup-common "1.0.4"
  :description "Hiccup tag generation functions"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[org.clojure/clojure "1.2.1"]]
  :aliases {"test-all"
            ["with-profile" "dev:clj,1.3:clj,1.4:clj,1.5:cljs" "test"]}
  :profiles
  {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
   :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :clj {:plugins [[com.keminglabs/cljx "0.3.2"]]
         :hooks [cljx.hooks]
         :cljx {:builds [{:source-paths ["src"]
                          :output-path "target/classes"
                          :rules :clj}
                         {:source-paths ["test"]
                          :output-path "target/test-classes"
                          :rules :clj}]}
         :source-paths ["target/classes"]
         :test-paths ["test" "target/test-classes"]}
   :dev {:plugins [[com.keminglabs/cljx "0.3.2"]]
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
         :source-paths ["target/classes"]
         :test-paths ["test" "target/test-classes"]
         :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}}
   :cljs {:dependencies [[org.clojure/clojure "1.5.1"]
                         [org.clojure/clojurescript "0.0-2138"]]
          :plugins [[com.cemerick/clojurescript.test "0.2.1"]
                    [lein-cljsbuild "1.0.1"]]
          :hooks [leiningen.cljsbuild]
          :cljx {:builds [{:source-paths ["src"]
                           :output-path "target/classes"
                           :rules :cljs}
                          {:source-paths ["test"]
                           :output-path "target/test-classes"
                           :rules :cljs}]}
          :cljsbuild {:builds [{:id "test"
                                :source-paths ["test" "target/classes"
                                               "target/test-classes"]
                                :compiler {:output-to "target/test/hiccup-common.js"
                                           :output-dir "target/test"
                                           :optimizations :whitespace
                                           :pretty-print true}}]
                      :test-commands {"phantom"
                                      ["phantomjs"
                                       :runner "target/test/hiccup-common.js"]}}
          :source-paths ["target/classes"]
          :test-paths ["test" "target/test-classes"]}})
