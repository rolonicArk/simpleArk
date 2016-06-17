(ns simpleArk.impl0
  (:require [clj-uuid :as uuid]
            [simpleArk.core :as ark]))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn update-property
  [ark journal-entry-uuid rolon-uuid property-name property-value])

(defn get-property-values
  [rolon-value]
  (::property-values rolon-value))

(defn get-property-journal-entry-uuids
  [rolon-value]
  (::property-journal-entry-uuids rolon-value))

(defn create-rolon-value
  "returns a new rolon value"
  [je-uuid rolon-uuid ps]
  (let [rolon-value (ark/->Rolon-value je-uuid rolon-uuid
                                       get-property-values get-property-journal-entry-uuids)
        rolon-value (assoc rolon-value ::property-values ps)
        rolon-value (assoc rolon-value ::property-journal-entry-uuids
                      (update-property-journal-entry-uuids (sorted-map) ps je-uuid))]
    rolon-value))

(defn get-rolon-values
  [rolon]
  (::rolon-values rolon))

(defn get-rolon
  [ark uuid]
  (if (= (uuid/get-version uuid) 1)
    ((::journal-entries ark) uuid)
    ((::other-rolons ark) uuid)))

(defn get-journal-entries
  [ark]
  (::journal-entries ark))

(defn create-rolon
  [ark je-uuid rolon-uuid property-values]
  (let [rolon (ark/->Rolon rolon-uuid get-rolon-values)
        rolon (assoc rolon ::rolon-values (sorted-map je-uuid
                                                      (create-rolon-value je-uuid rolon-uuid property-values)))]
    (if (= (uuid/get-version rolon-uuid) 1)
      (assoc-in ark [::journal-entries rolon-uuid] rolon)
      (assoc-in ark [::other-rolons rolon-uuid] rolon))))

(defn create-ark
  []
  (let [ark (ark/->Ark get-rolon get-journal-entries create-rolon nil update-property)
        ark (assoc ark ::journal-entries (sorted-map))
        ark (assoc ark ::other-rolons {})]
    ark))

(defn update-ark
  [ark registry je-uuid transaction-name s]
  (let [ark (create-rolon ark je-uuid je-uuid
                          {:classifier:transaction-name transaction-name
                           :descriptor:transaction-argument s})
        je (get-rolon ark je-uuid)
        f (registry transaction-name)
        ark (f ark je s)]
    ark))

(defrecord Db [ark-atom registry-atom]
  ark/Ark-db
  (get-ark [this]
    @ark-atom)
  (register-transaction! [this transaction-name f]
    (swap! registry-atom #(assoc % transaction-name f)))
  (process-transaction! [this transaction-name s]
    (let [je-uuid (uuid/v1)]
          (swap! ark-atom update-ark @registry-atom je-uuid transaction-name s)
          je-uuid)))

(defn create-ark-db
  "returns an ark db"
  []
  (let [ark (create-ark)
        ark-atom (atom ark)
        registry-atom (atom (sorted-map))
        ark-db (->Db ark-atom registry-atom)]
    ark-db))
