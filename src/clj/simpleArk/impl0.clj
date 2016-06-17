(ns simpleArk.impl0
  (:require [clj-uuid :as uuid]
            [simpleArk.core :as ark]))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn assoc-rolon
  [ark rolon-uuid rolon]
  (if (= (uuid/get-version rolon-uuid) 1)
    (assoc-in ark [::journal-entries rolon-uuid] rolon)
    (assoc-in ark [::other-rolons rolon-uuid] rolon)))

(defn update-property-
  [ark journal-entry-uuid rolon-uuid property-name property-value]
  (let [rolon (ark/get-rolon ark rolon-uuid)
        rolon-value (ark/get-latest-rolon-value rolon)
        ps (sorted-map property-name property-value)
        property-values (::property-values rolon-value)
        property-values (into property-values ps)
        rolon-value (assoc rolon-value ::property-values property-values)
        pjes (::property-journal-entry-uuids rolon-value)
        pjes (update-property-journal-entry-uuids pjes ps journal-entry-uuid)
        rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
        rolon (assoc-in rolon [::rolon-values journal-entry-uuid] rolon-value)
        ark (assoc-rolon ark rolon-uuid rolon)]
    ark))

(defn je-modified
  [ark journal-entry-uuid rolon-uuid]
  (let [je (ark/get-rolon ark journal-entry-uuid)
        je-value (ark/get-latest-rolon-value je)
        je-property-values (::property-values je-value)
        modified (:descriptor:modified je-property-values)
        modified (if modified
                   (cons modified rolon-uuid)
                   (sorted-set rolon-uuid))
        ark (update-property- ark journal-entry-uuid journal-entry-uuid :descriptor:modified modified)]
    ark))

(defn destroy-rolon
  [ark je-uuid rolon-uuid]
  (let [rolon (ark/get-rolon ark rolon-uuid)
        rolon-value (ark/get-latest-rolon-value rolon)
        property-values (::property-values rolon-value)
        property-values (reduce #(assoc %1 %2 nil) (sorted-map) (keys property-values))
        rolon-value (assoc rolon-value ::property-values property-values)
        pjes (::property-journal-entry-uuids rolon-value)
        pjes (reduce #(assoc %1 %2 je-uuid) (sorted-map) (keys pjes))
        rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
        rolon (assoc-in rolon [::rolon-values je-uuid] rolon-value)
        ark (assoc-rolon ark rolon-uuid rolon)
        ark (je-modified ark je-uuid rolon-uuid)]
    ark))

(defn je-modified
  [ark journal-entry-uuid rolon-uuid]
  (let [je (ark/get-rolon ark journal-entry-uuid)
        je-value (ark/get-latest-rolon-value je)
        je-property-values (::property-values je-value)
        modified (:descriptor:modified je-property-values)
        modified (if modified
                   (cons modified rolon-uuid)
                   (sorted-set rolon-uuid))
        ark (update-property- ark journal-entry-uuid journal-entry-uuid :descriptor:modified modified)]
    ark))

(defn update-property
  [ark journal-entry-uuid rolon-uuid property-name property-value]
  (let [ark (update-property- ark journal-entry-uuid rolon-uuid property-name property-value)
        ark (je-modified ark journal-entry-uuid rolon-uuid)]
    ark))

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
    (assoc-rolon ark rolon-uuid rolon)))

(defn create-ark
  []
  (let [ark (ark/->Ark get-rolon get-journal-entries create-rolon destroy-rolon update-property)
        ark (assoc ark ::journal-entries (sorted-map))
        ark (assoc ark ::other-rolons {})]
    ark))

(defn update-ark
  [ark registry je-uuid transaction-name s]
  (let [ark (create-rolon ark je-uuid je-uuid
                          {:classifier:transaction-name transaction-name
                           :descriptor:transaction-argument s})
        f (registry transaction-name)
        ark (f ark je-uuid s)]
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
