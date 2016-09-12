(ns simpleArk.ark-db0
  (:require [simpleArk.core :as ark-value]
            [simpleArk.log :as log]
            [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn open-ark!
  [ark-db]
  (ark-db/init-ark! ark-db (ark-value/create-ark ark-db)))

(defn process-transaction!
  ([ark-db transaction-name s]
  (let [je-uuid (uuid/journal-entry-uuid ark-db)]
    (ark-db/update-ark-db ark-db je-uuid transaction-name s)
    (log/info! ark-db :transaction transaction-name s)
    je-uuid))
  ([ark-db je-uuid transaction-name s]
   (ark-db/update-ark-db ark-db je-uuid transaction-name s)
   (log/info! ark-db :transaction transaction-name s)
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
