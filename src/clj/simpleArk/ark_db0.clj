(ns simpleArk.ark-db0
  (:require [simpleArk.core :as ark]
            [simpleArk.log :as log]
            [simpleArk.uuid :as uuid]))

(set! *warn-on-reflection* true)

(defn open-ark
  [ark-db]
  (reset! (::ark-atom ark-db) (ark/create-ark ark-db)))

(defn get-ark
  [ark-db]
  @(::ark-atom ark-db))

(defn process-transaction!
  [ark-db transaction-name s]
  (let [je-uuid (uuid/journal-entry-uuid ark-db)]
    (swap! (::ark-atom ark-db) ark/update-ark je-uuid transaction-name s)
    (log/info! ark-db :transaction transaction-name s)
    je-uuid))

(defn process-transaction-at!
  [ark-db je-uuid transaction-name s]
  (swap! (::ark-atom ark-db) ark/update-ark je-uuid transaction-name s)
  (log/info! ark-db :transaction transaction-name s))

(defn- build
  "returns an ark db"
  [m]
  (let [ark-atom (atom nil)
        ark-db (-> m
                   (assoc ::ark-atom ark-atom)
                   (assoc :ark-db/open-ark open-ark)
                   (assoc :ark-db/get-ark get-ark)
                   (assoc :ark-db/process-transaction! process-transaction!)
                   (assoc :ark-db/process-transaction-at! process-transaction-at!)
                   )]
    ark-db))

(defn builder
  []
  build)
