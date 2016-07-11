(ns simpleArk.closer-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]
            [clojure.core.async :as async]
            [simpleArk.closer :refer :all]))

(set! *warn-on-reflection* true)

(deftest closer
  (defn close-a [this] (info this "close a"))
  (defn close-b [this] (info this "close b"))
  (defn close-c [this] (info this "close c"))

  (let [this (-> {}
                 (logt/build)
                 (logt/set-log-chan (async/chan 100))
                 (open-component "a" close-a)
                 (open-component "b" close-b)
                 (open-component "c" close-c))]
    (is (= [:log/info (list "opening a")] (logt/get-msg this)))
    (is (= [:log/info (list "opening b")] (logt/get-msg this)))
    (is (= [:log/info (list "opening c")] (logt/get-msg this)))
    (close-all this)
    (is (= [:log/info (list "closing c")] (logt/get-msg this)))
    (is (= [:log/info (list "close c")] (logt/get-msg this)))
    (is (= [:log/info (list "closing b")] (logt/get-msg this)))
    (is (= [:log/info (list "close b")] (logt/get-msg this)))
    (is (= [:log/info (list "closing a")] (logt/get-msg this)))
    (is (= [:log/info (list "close a")] (logt/get-msg this)))
    ))
