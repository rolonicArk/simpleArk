(ns simpleArk.ark-dba0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]
            [simpleArk.logt :as logt]
            [simpleArk.uuid :as uuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.reg0 :as reg0]
            [simpleArk.ark-dba0 :as ark-dbaa0]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(deftest ark-dba0
  (def ark-db ((comp
                 (ark-dbaa0/builder)
                 (impl0/builder)
                 (reg0/builder)
                 (uuidi/builder)
                 (logt/builder))
                {}))
  (open-ark ark-db)
  ;(test0 ark-db)
  (closer/close-all ark-db)
  )
