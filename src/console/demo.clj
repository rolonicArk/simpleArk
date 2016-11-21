(ns console.demo
  (:require [console.server :as console]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            ))

(defmethod ark-value/eval-transaction :hello-world!
  [ark-value ark-db n s]
  (println "Hello," s)
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)]
    (ark-value/update-property ark-value ark-db je-uuid [:index/headline] "Just for fun!")))

(defmethod ark-value/eval-transaction :trouble!
  [ark-value ark-db n s]
  (println "throwing exception")
  (throw (new IllegalArgumentException "A troublesome transaction")))

(def ark-db console/ark-db)

(defn initializer
  []
  (console/initializer)

  (ark-db/open-ark! ark-db)
  (ark-db/process-transaction! ark-db :hello-world! "Fred")
  (console/update-ark-record! (ark-db/get-ark-record ark-db))
  )
