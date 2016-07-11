(ns simpleArk.closer-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]
            [clojure.core.async :as async]
            [simpleArk.closer :refer :all]))

(set! *warn-on-reflection* true)

(deftest closer
  (close-all {})

  (defn close-a [this] (info this "close a"))
  (defn close-b [this] (info this "close b"))
  (defn close-c [this] (info this "close c"))

  (let [this (-> {}
                 (logt/build)
                 (logt/set-log-chan (async/chan 100))
                 (open-component "a" close-a)
                 (open-component "b" close-b)
                 (open-component "c" close-c))]
    (println (keys this))
    (info this "first close")
    (is (= [:log/info (list "opening a")] (logt/get-msg this)))
    (close-all this)
    (info this "second close")
    (close-all this)
    (println (logt/get-msg this))
    (println (logt/get-msg this))
    (println (logt/get-msg this))
    (println (logt/get-msg this))
    (println (logt/get-msg this))
    ))
