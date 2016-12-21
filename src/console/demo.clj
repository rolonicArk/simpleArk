(ns console.demo
  (:require [console.server :as console]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.uuid :as suuid]))

(defmethod ark-value/eval-transaction :trouble!
  [ark-value ark-db n s]
  (println "throwing exception")
  (throw (new IllegalArgumentException "A troublesome transaction")))

(defmethod ark-value/eval-transaction :hello-world!
  [ark-value ark-db n s]
  (println "Hello," s)
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)]
    (ark-value/update-property ark-value ark-db je-uuid [:index/headline] "Just for fun!")))

(def ark-db console/ark-db)

(def welcome-uuid (suuid/random-uuid ark-db))
(def profile-uuid (suuid/random-uuid ark-db))
(def contacts-uuid (suuid/random-uuid ark-db))
(def console-uuid (suuid/random-uuid ark-db))
(def fred-uuid (suuid/random-uuid ark-db))
(def sam-uuid (suuid/random-uuid ark-db))
(def kris-uuid (suuid/random-uuid ark-db))

(defn initializer
  []
  (console/initializer)
  (ark-db/open-ark! ark-db)

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [welcome-uuid
       {[:index/headline] "create welcome capability"}
       {[:index/headline] "Welcome Capability"
        [:index/name] "welcome-capability"
        [:index/capability.name] "welcome"}]))

  (console/update-ark-record! (ark-db/get-ark-record ark-db))
  )
