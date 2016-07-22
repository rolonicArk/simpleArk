(ns simpleArk.ark-dba0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]
            [simpleArk.log0 :as log0]
            [simpleArk.uuid :as uuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.reg0 :as reg0]
            [simpleArk.ark-dba0 :as ark-dba0]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(deftest ark-dba0
  (def ark-db ((comp
                 (ark-dba0/builder)
                 (impl0/builder)
                 (reg0/builder)
                 (uuidi/builder)
                 (closer/builder)
                 (log0/builder))
                {}))
  (open-ark ark-db)
  ;(test0 ark-db)
  (closer/close-all ark-db)
  )
