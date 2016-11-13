(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.miMap :as miMap]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-db & keyvals]
  (apply miMap/new-MI-map keyvals))

(defn update-ark
  [ark-value ark-db je-uuid transaction-name s]
  (let [ark-value (-> ark-value
                      (assoc :latest-journal-entry-uuid je-uuid)
                      (ark-value/make-rolon ark-db
                                            je-uuid
                                            (create-mi
                                              ark-db
                                              [:index/transaction-name] transaction-name
                                              [:content/transaction-argument] s))
                      (ark-value/eval-transaction ark-db transaction-name s))]
    (if (:selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn create-ark
  [ark-db]
  (-> (ark-value/->Ark-value ark-db)
      (ark-value/init-ark-value ark-db)))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc :ark-value/create-mi create-mi)
        (assoc :ark-value/update-ark update-ark)
        (assoc :ark-value/create-ark create-ark))))
