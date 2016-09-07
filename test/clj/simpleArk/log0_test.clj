(ns simpleArk.log0-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.log0 :as log0]))

(set! *warn-on-reflection* true)

(deftest log0
  (def c ((comp
            (log0/builder))
           {}))
  (error! c 1 2 3)
  )
