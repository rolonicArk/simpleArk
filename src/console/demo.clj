(ns console.demo
  (:require [console.server :as console]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.log :as log]
            [simpleArk.logt :as logt]
            [simpleArk.uuid :as uuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.ark-db0 :as ark-db0]
            [simpleArk.closer :as closer]
            [simpleArk.mapish :as mapish]
            [simpleArk.reader :as reader]
            [simpleArk.miMap :as miMap]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]
            [tiples.server :as tiples]
            ))

(defmethod ark-value/eval-transaction ::hello-world!
  [ark-value ark-db n s]
  (println "Hello," s)
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)]
    (ark-value/update-property ark-value ark-db je-uuid [:index/headline] "Just for fun!")))

(def ark-db ((comp
               (ark-db/builder)
               (ark-db0/builder)
               (ark-value0/builder)
               (uuidi/builder)
               (closer/builder)
               (logt/builder)
               (reader/builder))
              {}))

(defn initializer
  []
  (miMap/register ark-db)
  (arkRecord/register ark-db)
  (rolonRecord/register ark-db)
  (ark-db/open-ark! ark-db)
  (ark-db/process-transaction! ark-db ::hello-world! "Fred")
  (console/update-ark-record! (ark-db/get-ark-value ark-db)))
