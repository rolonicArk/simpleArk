(ns simpleArk.ark-dba1-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.log0 :as log0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.ark-dba1 :as ark-dba1]
            [simpleArk.tlog0 :as tlog0]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(defmethod eval-transaction ::trouble!
  [n s]
  (println "throwing exception")
  (throw (new IllegalArgumentException)))

(defmethod eval-transaction ::hello-world!
  [n s]
  (println "Hello," s)
  (let [je-uuid (get-current-journal-entry-uuid)]
    (update-property! je-uuid :classifier/headline "Just for fun!")))

(defn ark-dba0-test
  [ark-db]
  (try
    (ark-db/process-transaction! ark-db ::trouble! "eek!")
    (println ">>> did not receive expected exception")
    (catch Exception e))
  (ark-db/process-transaction! ark-db ::hello-world! "Fred")
  (ark-db/process-transaction! ark-db ::hello-world! "Sam")
  (ark-db/process-transaction! ark-db ::hello-world! "Ruth")

  (first (keep println (tran-seq ark-db)))
  )

(deftest ark-dba0
  (def ark-db ((comp
                 (ark-db/builder)
                 (ark-dba1/builder)
                 (tlog0/builder)
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
