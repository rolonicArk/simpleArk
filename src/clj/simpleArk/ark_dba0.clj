(ns simpleArk.ark-dba0
  (:require [simpleArk.core :as ark]
            [simpleArk.log :as log]
            [simpleArk.uuid :as uuid]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(defn open-ark
  [ark-db]
  (reset! (::ark-atom ark-db) (ark/create-ark ark-db)))

(defn get-ark
  [ark-db]
  @(::ark-atom ark-db))

(defn process-transaction!
  ([ark-db transaction-name s]
   (let [je-uuid (uuid/journal-entry-uuid ark-db)]
     (swap! (::ark-atom ark-db) ark/update-ark je-uuid transaction-name s)
     (log/info! ark-db :transaction transaction-name s)
     je-uuid))
  ([ark-db je-uuid transaction-name s]
   (swap! (::ark-atom ark-db) ark/update-ark je-uuid transaction-name s)
   (log/info! ark-db :transaction transaction-name s)
   je-uuid))

(defn builder [& {:keys [tran-chan]
                  :or {tran-chan (async/chan 100)}}]
  (fn [m]
    (-> m
        (assoc ::tran-chan chan)
        (assoc ::ark-atom (atom nil))
        (assoc :ark-db/open-ark open-ark)
        (assoc :ark-db/get-ark get-ark)
        (assoc :ark-db/process-transaction! process-transaction!))))
