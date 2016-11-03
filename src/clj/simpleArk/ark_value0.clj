(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.uuid :as uuid]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-value & keyvals]
  (apply mapish/new-MI-map keyvals))

(defn make-rolon
  [ark-value rolon-uuid properties]
  (let [ark-value
        (if (ark-value/get-rolon ark-value rolon-uuid)
          ark-value
          (ark-value/assoc-rolon
            ark-value
            rolon-uuid
            (ark-value/->Rolon rolon-uuid)))]
    (ark-value/update-properties ark-value rolon-uuid properties)))

(defn update-ark
  [ark-value je-uuid transaction-name s]
  (let [ark-value (-> ark-value
                      (assoc :latest-journal-entry-uuid je-uuid)
                      (ark-value/make-rolon je-uuid
                                            (create-mi
                                              ark-value
                                              [:classifier/transaction-name] transaction-name
                                              [:descriptor/transaction-argument] s))
                      (ark-value/eval-transaction transaction-name s))]
    (if (:selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn create-ark
  [this-db]
  (-> (ark-value/->Ark-value this-db make-rolon update-ark create-mi)
      (ark-value/init-ark-value)))

(defn- build
  "returns an ark db"
  [m]
  (assoc m :ark-value/create-ark create-ark))

(defn builder
  []
  build)
