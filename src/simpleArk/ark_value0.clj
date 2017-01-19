(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.miMap :as miMap]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-db & keyvals]
  (apply miMap/new-MI-map keyvals))

(defn update-ark
  [ark-record ark-db user-uuid je-uuid transaction-name s]
  (let [ark-record (assoc ark-record :latest-journal-entry-uuid je-uuid)
        ark-record (ark-value/make-rolon
                    ark-record
                    ark-db
                    je-uuid
                    (create-mi
                      ark-db
                      [:index/transaction-name] transaction-name
                      [:content/transaction-argument] s))
        ark-record (if (nil? user-uuid)
                    ark-record
                    (ark-value/update-relation
                      ark-record
                      ark-db
                      "transaction"
                      user-uuid
                      je-uuid
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
