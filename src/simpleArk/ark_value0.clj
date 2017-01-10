(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.miMap :as miMap]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-db & keyvals]
  (apply miMap/new-MI-map keyvals))

(defn update-ark
  [ark-value ark-db user-uuid je-uuid transaction-name s]
  (let [ark-value (assoc ark-value :latest-journal-entry-uuid je-uuid)
        ark-value (ark-value/make-rolon
                    ark-value
                    ark-db
                    je-uuid
                    (create-mi
                      ark-db
                      [:index/transaction-name] transaction-name
                      [:content/transaction-argument] s))
        ark-value (if (nil? user-uuid)
                    ark-value
                    (let [ark-value
                          (ark-value/update-property
                            ark-value
                            ark-db
                            je-uuid
                            [:inv-rel/transaction user-uuid]
                            user-uuid)
                          ark-value
                          (ark-value/update-property
                            ark-value
                            ark-db
                            user-uuid
                            [:rel/transaction je-uuid]
                            je-uuid)]
                      ark-value))
        ark-value (ark-value/eval-transaction ark-value ark-db transaction-name s)]
    (if (:selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc :ark-value/create-mi create-mi)
        (assoc :ark-value/update-ark update-ark))))
