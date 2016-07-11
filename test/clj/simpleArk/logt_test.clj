(ns simpleArk.logt-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]))

(deftest logt
  (def c (-> {}
             (logt/build)
             (logt/set-log-chan (async/chan 3))))
  (warn c 1 2 3)
  (def l1 (logt/get-msg c))
  (is (= (l1 0) :log/warn))
  (is (= (l1 1) "(1 2 3)"))
  )
