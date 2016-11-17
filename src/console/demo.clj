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
            ))

(def ark-db ((comp
               (ark-db/builder)
               (ark-db0/builder)
               (ark-value0/builder)
               (uuidi/builder)
               (closer/builder)
               (logt/builder)
               (reader/builder))
              {}))
(ark-db/open-ark! ark-db)

(console/update-ark-record! (ark-db/get-ark-value ark-db))
