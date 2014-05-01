(defproject hiccup "2.0.0-SNAPSHOT"
  :description "A fast library for rendering HTML in Clojure"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[hiccup/hiccup-common "2.0.0-SNAPSHOT"]
                 [hiccup/hiccup-common "2.0.0-SNAPSHOT"]
                 [hiccup/hiccup-middleware "2.0.0-SNAPSHOT"]]
  :plugins [[codox "0.6.6"]
            [lein-sub "0.2.4"]]
  :codox {:exclude [hiccup.compiler]
          :src-dir-uri "http://github.com/weavejester/hiccup/blob/2.0.0/"
          :src-linenum-anchor-prefix "L"
          :sources ["hiccup-common/src"
                    "hiccup-core/src"
                    "hiccup-middleware/src"]}
  :sub ["hiccup-common"
        "hiccup-core"
        "hiccup-middleware"])
