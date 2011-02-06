(ns hiccup.middleware
  "Ring middleware functions for Hiccup."
  (:use hiccup.core))

(defn wrap-base-url
  "Ring middleware that wraps the handler in the with-base-url function."
  [handler url]
  (fn [request]
    (with-base-url url
      (handler request))))
