(ns simpleArk.ark-dba1-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]
            [simpleArk.log0 :as log0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.reg0 :as reg0]
            [simpleArk.ark-dba1 :as ark-dba1]
            [simpleArk.tlog0 :as tlog0]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(defn trouble!
  "transaction throws an exception"
  [s]
  (println "throwing exception")
  (throw (new IllegalArgumentException)))

(defn hello-world!
  "simple transaction test"
  [s]
  (println "Hello," s)
  (let [je-uuid (get-current-journal-entry-uuid)]
    (update-property! je-uuid :classifier/headline "Just for fun!")))

(defn ark-dba0-test
  [ark-db]
  (register-transaction! ark-db ::trouble! trouble!)
  (register-transaction! ark-db ::hello-world! hello-world!)
  (try
    (process-transaction! ark-db ::trouble! "eek!")
    (println ">>> did not receive expected exception")
    (catch Exception e))
  (process-transaction! ark-db ::hello-world! "Fred")
  (process-transaction! ark-db ::hello-world! "Sam")
  (process-transaction! ark-db ::hello-world! "Ruth")

  (first (keep println (tran-seq ark-db)))
  )

(deftest ark-dba0
  (def ark-db ((comp
                 (ark-dba1/builder)
                 (tlog0/builder)
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
