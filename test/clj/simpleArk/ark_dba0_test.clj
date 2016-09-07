(ns simpleArk.ark-dba0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.log0 :as log0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.ark-dba0 :as ark-dba0]
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
    (process-transaction! ark-db ::trouble! "eek!")
    (println ">>> did not receive expected exception")
    (catch Exception e))
  (process-transaction! ark-db ::hello-world! "Fred")
  (process-transaction! ark-db ::hello-world! "Sam")
  (process-transaction! ark-db ::hello-world! "Ruth")
  )

(deftest ark-dba0
  (def ark-db ((comp
                 (ark-dba0/builder)
                 (ark-value0/builder)
                 (uuidi/builder)
                 (closer/builder)
                 (log0/builder))
                {}))
  (open-ark ark-db)
  (ark-dba0-test ark-db)
  (closer/close-all ark-db)
  )
