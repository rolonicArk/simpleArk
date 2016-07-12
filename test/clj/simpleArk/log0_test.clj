(ns simpleArk.log0-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.log0 :as log0]))

(set! *warn-on-reflection* true)

(deftest log0
  (def c (log0/build {}))
  (println (keys c))
  (error! c 1 2 3)
  )
