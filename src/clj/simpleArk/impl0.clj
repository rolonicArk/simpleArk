(ns simpleArk.impl0
  (:require [clj-uuid :as uuid]
            [simpleArk.core :as ark]))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn get-property-values
  [rolon-value]
  (::property-values rolon-value))

(defn get-property-journal-entry-uuids
  [rolon-value]
  (::property-journal-entry-uuids rolon-value))

(defn create-rolon-value
  "returns a new rolon value"
  [rolon-uuid je-uuid ps]
  (let [rolon-value (ark/->Rolon-value rolon-uuid je-uuid
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
  [ark rolon-uuid je-uuid property-values]
  (let [rolon (ark/->Rolon rolon-uuid get-rolon-values)
        rolon (assoc rolon ::rolon-values (sorted-map je-uuid
                                                      (create-rolon-value rolon-uuid je-uuid property-values)))
        ark (assoc ark rolon-uuid rolon)]
    ark))

(defn create-ark
  []
  (let [ark (ark/->Ark get-rolon get-journal-entries create-rolon nil nil)
        ark (assoc ark ::journal-entries (sorted-map))
        ark (assoc ark ::other-rolons {})]
    ark))

(defn update-ark
  [ark registry transaction-name s]
  (let [je-uuid (uuid/v1)
        ark (create-rolon ark je-uuid je-uuid
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
    (swap! ark-atom update-ark @registry-atom transaction-name s)))

(defn create-ark-db
  "returns an ark db"
  []
  (let [ark (create-ark)
        ark-atom (atom ark)
        registry-atom (atom (sorted-map))
        ark-db (->Db ark-atom registry-atom)]
    ark-db))
