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

(defn hello-world!
  "simple transaction test"
  [s]
  (println "Hello," s)
  (let [je-uuid (get-current-journal-entry-uuid)]
    (update-property! je-uuid :classifier/headline "Just for fun!")))

(defn ark-dba0-test
  [ark-db]
  (register-transaction! ark-db ::hello-world! hello-world!)
  (def hello-je-uuid (process-transaction! ark-db ::hello-world! "Fred"))
  )

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
  (ark-dba0-test ark-db)
  (closer/close-all ark-db)
  )
