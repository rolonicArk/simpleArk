(ns simpleArk.ark-dba0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]
            [simpleArk.log0 :as log0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.reg0 :as reg0]
            [simpleArk.ark-dba0 :as ark-dba0]
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
  (println ">>>" (.toString (process-transaction! ark-db ::trouble! "eek!")))
  (println ">>>" (process-transaction! ark-db ::hello-world! "Fred"))
  (println ">>>" (process-transaction! ark-db ::hello-world! "Sam"))
  (println ">>>" (process-transaction! ark-db ::hello-world! "Ruth"))
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
