(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.uuid :as uuid]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-value & keyvals]
  (apply mapish/new-MI-map keyvals))

(defn update-ark
  [ark-value je-uuid transaction-name s]
  (let [ark-value (-> ark-value
                      (assoc :latest-journal-entry-uuid je-uuid)
                      (ark-value/make-rolon je-uuid
                                            (create-mi
                                              ark-value
                                              [:index/transaction-name] transaction-name
                                              [:content/transaction-argument] s))
                      (ark-value/eval-transaction transaction-name s))]
    (if (:selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn create-ark
  [this-db]
  (-> (ark-value/->Ark-value this-db update-ark create-mi)
      (ark-value/init-ark-value)))

(defn- build
  "returns an ark db"
  [m]
  (assoc m :ark-value/create-ark create-ark))

(defn builder
  []
  build)
