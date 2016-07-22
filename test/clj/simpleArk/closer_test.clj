(ns simpleArk.closer-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(deftest closer
  (let [this ((comp
                (closer/builder)
                (logt/builder)) {})]
    (closer/open-component this "a" #(info! % "close a"))
    (closer/open-component this "b" #(info! % "close b"))
    (closer/open-component this "c" #(info! % "close c"))
    (is (= [:log/info! "opening a"] (logt/get-msg this)))
    (is (= [:log/info! "opening b"] (logt/get-msg this)))
    (is (= [:log/info! "opening c"] (logt/get-msg this)))
    (closer/close-all this)
    (is (= [:log/info! "closing c"] (logt/get-msg this)))
    (is (= [:log/info! "close c"] (logt/get-msg this)))
    (is (= [:log/info! "closing b"] (logt/get-msg this)))
    (is (= [:log/info! "close b"] (logt/get-msg this)))
    (is (= [:log/info! "closing a"] (logt/get-msg this)))
    (is (= [:log/info! "close a"] (logt/get-msg this)))
    ))
