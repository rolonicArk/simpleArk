(ns simpleArk.arkDb.ark-db0
  (:require [simpleArk.arkValue.ark-value :as ark-value]
            [simpleArk.log.log :as log]
            [simpleArk.uuid :as uuid]
            [simpleArk.arkDb.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn open-ark!
  [ark-db]
  (ark-db/init-ark-db! ark-db (ark-value/create-ark ark-db)))

(defn process-transaction!
  ([ark-db user-uuid capability transaction-name s]
   (let [je-uuid (uuid/journal-entry-uuid ark-db)]
     (ark-db/update-ark-db ark-db user-uuid capability je-uuid transaction-name s)
     (log/info! ark-db :transaction user-uuid capability transaction-name s)
     (ark-db/process-notifications ark-db je-uuid)
     je-uuid))
  ([ark-db user-uuid capability je-uuid transaction-name s]
   (ark-db/update-ark-db ark-db user-uuid capability je-uuid transaction-name s)
   (log/info! ark-db :transaction user-uuid capability transaction-name s)
   je-uuid))

(defn- build
  "returns an ark db"
  [m]
  (-> m
      (assoc :ark-db/open-ark! open-ark!)
      (assoc :ark-db/process-transaction! process-transaction!)))

(defn builder
  []
  build)
