(ns simpleArk.logt-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]))

(set! *warn-on-reflection* true)

(deftest logt
  (def c (-> {}
             (logt/build)
             (logt/set-log-chan (async/chan 3))))
  (warn c 1 2 3)
  (def l1 (logt/get-msg c))
  (is (= [:log/warn 1 2 3] l1)))
