(ns simpleArk.closer-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]
            [clojure.core.async :as async]
            [simpleArk.closer :refer :all]))

(set! *warn-on-reflection* true)

(deftest closer
  (defn close-a [this] (info! this "close a"))
  (defn close-b [this] (info! this "close b"))
  (defn close-c [this] (info! this "close c"))

  (let [this (-> {}
                 (logt/build)
                 (logt/set-log-chan (async/chan 100))
                 (open-component "a" close-a)
                 (open-component "b" close-b)
                 (open-component "c" close-c))]
    (is (= [:log/info! "opening a"] (logt/get-msg this)))
    (is (= [:log/info! "opening b"] (logt/get-msg this)))
    (is (= [:log/info! "opening c"] (logt/get-msg this)))
    (close-all this)
    (is (= [:log/info! "closing c"] (logt/get-msg this)))
    (is (= [:log/info! "close c"] (logt/get-msg this)))
    (is (= [:log/info! "closing b"] (logt/get-msg this)))
    (is (= [:log/info! "close b"] (logt/get-msg this)))
    (is (= [:log/info! "closing a"] (logt/get-msg this)))
    (is (= [:log/info! "close a"] (logt/get-msg this)))
    ))
