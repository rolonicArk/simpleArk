(ns simpleArk.logt-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]
            [simpleArk.log.log :refer :all]
            [simpleArk.log.log :as log]
            [simpleArk.log.logt :as logt]))

(set! *warn-on-reflection* true)

(def c ((comp
          (logt/builder :chan (async/chan 3)))
         {}))

(deftest logt
  (warn! c 1 2 3)
  (def l1 (log/get-msg c))
  (is (= [:log/warn! 1 2 3] l1)))
