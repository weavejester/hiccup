(defproject hiccup "1.0.5"
  :description "A fast library for rendering HTML in Clojure"
  :url "http://github.com/weavejester/hiccup"
  :dependencies [[hiccup/hiccup-common "1.0.5"]
                 [hiccup/hiccup-compiler "1.0.5"]]
  :plugins [[codox "0.6.6"]
            [lein-sub "0.2.4"]]
  :codox {:sources ["hiccup-common/src"
                    "hiccup-compiler/src"]
          :exclude [hiccup.compiler]
          :src-dir-uri "http://github.com/weavejester/hiccup/blob/1.0.5/"
          :src-linenum-anchor-prefix "L"}
  :sub ["hiccup-common"
        "hiccup-compiler"])
