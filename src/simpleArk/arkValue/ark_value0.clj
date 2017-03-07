(ns simpleArk.arkValue.ark-value0
  (:require [simpleArk.arkValue.ark-value :as ark-value]
            [simpleArk.miMap :as miMap]
            [simpleArk.arkRecord :as arkRecord]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-db & keyvals]
  (apply miMap/new-MI-map keyvals))

(defn update-ark
  [ark-record ark-db user-uuid capability je-uuid transaction-name s]
  (let [ark-record (assoc ark-record :latest-journal-entry-uuid je-uuid)
        ark-record (ark-value/make-rolon
                     ark-record
                     ark-db
                     je-uuid
                     (create-mi
                       ark-db
                       [:index/transaction-name] transaction-name
                       [:edn-transaction/transaction-argument] s))
        ark-record (if (nil? user-uuid)
                     ark-record
                     (ark-value/update-relation-
                       ark-record
                       ark-db
                       "transaction"
                       user-uuid
                       je-uuid
                       false
                       true))
        capability-uuid (arkRecord/capability-uuid ark-record capability)
        ark-record (if (nil? capability-uuid)
                     ark-record
                     (ark-value/update-relation-
                         ark-record
                         ark-db
                         "source-capability"
                         je-uuid
                         capability-uuid
                         false
                         true))
        ark-record (ark-value/eval-transaction ark-record ark-db transaction-name s)]
    (if (:selected-time ark-record)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-record))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc :ark-value/create-mi create-mi)
        (assoc :ark-value/update-ark update-ark))))
