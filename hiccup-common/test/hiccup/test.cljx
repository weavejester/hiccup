(ns hiccup.test
  #+cljs (:import goog.Uri))

#+cljs
(extend-type goog.Uri
  IEquiv
  (-equiv [uri other]
    (= (.toString uri) (.toString other))))
