(ns hiccup.middleware
  "Ring middleware functions for Hiccup."
  (:require [hiccup.util :as util]))

(defn wrap-base-url
  "Ring middleware that wraps the handler in the [[with-base-url]] function.
  The base URL may be specified as an argument. Otherwise, the `:context` key
  on the request map is used."
  ([handler]
   (wrap-base-url handler nil))
  ([handler base-url]
   (fn [request]
     (util/with-base-url (or base-url (:context request))
       (handler request)))))
