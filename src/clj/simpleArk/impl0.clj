(ns simpleArk.impl0
  (:require [simpleArk.core :as ark]))

(defn get-latest-journal-entry-uuid
  [ark]
  (::latest-journal-entry-uuid ark))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn assoc-rolon
  [ark rolon-uuid rolon]
  (cond
    (ark/journal-entry-uuid? rolon-uuid) (assoc-in ark [::journal-entries rolon-uuid] rolon)
    (ark/index-uuid? rolon-uuid) (assoc-in ark [::indexes rolon-uuid] rolon)
    (ark/random-uuid? rolon-uuid) (assoc-in ark [::random-rolons rolon-uuid] rolon)
    :else (throw (Exception. (str rolon-uuid " is unrecognized")))))

(defn update-properties-
  [ark journal-entry-uuid rolon-uuid properties]
  (let [rolon (ark/get-rolon ark rolon-uuid)
        rolon-value (ark/get-latest-rolon-value rolon)
        property-values (::property-values rolon-value)
        property-values (into property-values properties)
        rolon-value (assoc rolon-value ::property-values property-values)
        pjes (::property-journal-entry-uuids rolon-value)
        pjes (update-property-journal-entry-uuids pjes properties journal-entry-uuid)
        rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
        rolon (assoc-in rolon [::rolon-values journal-entry-uuid] rolon-value)
        ark (assoc-rolon ark rolon-uuid rolon)]
    ark))

(defn update-property-
  [ark journal-entry-uuid rolon-uuid property-name property-value]
  (update-properties- ark journal-entry-uuid rolon-uuid (sorted-map property-name property-value)))

(defn je-modified
  "track the rolons modified by the journal entry"
  [ark journal-entry-uuid rolon-uuid]
  (let [je (ark/get-rolon ark journal-entry-uuid)
        je-value (ark/get-latest-rolon-value je)
        je-property-values (::property-values je-value)
        modified (:descriptor:modified je-property-values)
        modified (if modified
                   (conj modified rolon-uuid)
                   (sorted-set rolon-uuid))
        ark (update-property- ark journal-entry-uuid journal-entry-uuid :descriptor:modified modified)]
    ark))

(defn destroy-rolon
  [ark rolon-uuid]
  (let [je-uuid (get-latest-journal-entry-uuid ark)
        rolon (ark/get-rolon ark rolon-uuid)
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

(defn update-properties
  [ark rolon-uuid properties]
  (let [journal-entry-uuid (get-latest-journal-entry-uuid ark)
        ark (update-properties- ark journal-entry-uuid rolon-uuid properties)
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
  (cond
    (ark/journal-entry-uuid? uuid) ((::journal-entries ark) uuid)
    (ark/index-uuid? uuid) ((::indexes ark) uuid)
    (ark/random-uuid? uuid) ((::random-rolons ark) uuid)
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn get-journal-entries
  [ark]
  (::journal-entries ark))

(defn get-indexes
  [ark]
  (::indexes ark))

(defn get-random-rolons
  [ark]
  (::random-rolons ark))

(defn create-rolon
  [ark rolon-uuid property-values]
  (let [je-uuid (get-latest-journal-entry-uuid ark)
        rolon (ark/->Rolon rolon-uuid get-rolon-values)
        rolon (assoc rolon ::rolon-values (sorted-map je-uuid
                                                      (create-rolon-value je-uuid rolon-uuid property-values)))
        ark (assoc-rolon ark rolon-uuid rolon)
        ark (je-modified ark je-uuid rolon-uuid)]
    ark))

(defn create-ark
  []
  (let [ark (ark/->Ark get-rolon get-journal-entries get-indexes get-random-rolons
                       create-rolon destroy-rolon update-properties
                       get-latest-journal-entry-uuid)
        ark (assoc ark ::journal-entries (sorted-map))
        ark (assoc ark ::indexes (sorted-map))
        ark (assoc ark ::random-rolons {})]
    ark))

(defn update-ark
  [ark registry je-uuid transaction-name s]
  (let [ark (assoc ark ::latest-journal-entry-uuid je-uuid)
        ark (create-rolon ark je-uuid
                          {:classifier:transaction-name transaction-name
                           :descriptor:transaction-argument s})
        f (registry transaction-name)
        ark (f ark s)]
    ark))

(defrecord Db [ark-atom registry-atom]
  ark/Ark-db
  (get-ark [this]
    @ark-atom)
  (register-transaction! [this transaction-name f]
    (swap! registry-atom #(assoc % transaction-name f)))
  (process-transaction! [this transaction-name s]
    (let [je-uuid (ark/journal-entry-uuid)]
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
