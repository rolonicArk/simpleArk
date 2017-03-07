(ns simpleArk.closer-test
  (:require [clojure.test :refer :all]
            [simpleArk.log.log :refer :all]
            [simpleArk.log.log :as log]
            [simpleArk.log.logt :as logt]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(deftest closer
  (let [this ((comp
                (closer/builder)
                (logt/builder)) {})]
    (closer/open-component this "a" #(info! % "close a"))
    (closer/open-component this "b" #(info! % "close b"))
    (closer/open-component this "c" #(info! % "close c"))
    (is (= [:log/info! "opening a"] (log/get-msg this)))
    (is (= [:log/info! "opening b"] (log/get-msg this)))
    (is (= [:log/info! "opening c"] (log/get-msg this)))
    (closer/close-all this)
    (is (= [:log/info! "closing c"] (log/get-msg this)))
    (is (= [:log/info! "close c"] (log/get-msg this)))
    (is (= [:log/info! "closing b"] (log/get-msg this)))
    (is (= [:log/info! "close b"] (log/get-msg this)))
    (is (= [:log/info! "closing a"] (log/get-msg this)))
    (is (= [:log/info! "close a"] (log/get-msg this)))
    ))
