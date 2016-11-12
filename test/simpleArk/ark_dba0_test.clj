(ns simpleArk.ark-dba0-test
  (:require [clojure.test :refer :all]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.log0 :as log0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.ark-dba0 :as ark-dba0]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(defmethod ark-value/eval-transaction ::trouble!
  [ark-value ark-db n s]
  (println "throwing exception")
  (throw (new IllegalArgumentException)))

(defmethod ark-value/eval-transaction ::hello-world!
  [ark-value ark-db n s]
  (println "Hello," s)
  (let [je-uuid (ark-value/get-latest-journal-entry-uuid ark-value)]
    (ark-value/update-property ark-value ark-db je-uuid [:index/headline] "Just for fun!")))

(defn ark-dba0-test
  [ark-db]
  (try
    (ark-db/process-transaction! ark-db ::trouble! "eek!")
    (println ">>> did not receive expected exception")
    (catch Exception e
      (println "got expected exception")))
  (ark-db/process-transaction! ark-db ::hello-world! "Fred")
  (ark-db/process-transaction! ark-db ::hello-world! "Sam")
  (ark-db/process-transaction! ark-db ::hello-world! "Ruth")
  )

(deftest ark-dba0
  (def ark-db ((comp
                 (ark-db/builder)
                 (ark-dba0/builder)
                 (ark-value0/builder)
                 (uuidi/builder)
                 (closer/builder)
                 (log0/builder))
                {}))
  (ark-db/open-ark! ark-db)
  (try
    (ark-dba0-test ark-db)
    (finally
      (closer/close-all ark-db))))
