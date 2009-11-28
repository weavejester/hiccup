(ns test.main
  (:use clojure.contrib.test-is)
  (:require test.hiccup)
  (:require test.hiccup.form-helpers))

(run-tests 'test.hiccup
           'test.hiccup.form-helpers)
