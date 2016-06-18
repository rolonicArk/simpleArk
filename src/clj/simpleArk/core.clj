(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defprotocol Ark-db
  (get-ark [this]
    "returns the current value of the ark")
  (register-transaction! [this transaction-name f]
    "defines a transaction,
    where f takes an ark and an (edn) string,
    and then returns a revised ark")
  (process-transaction! [this transaction-name s]
    "process a transaction with an (edn) string,
    returning the new journal-entry uuid"))

(defrecord Ark [get-rolon get-journal-entries get-other-rolons create-rolon destroy-rolon update-property get-latest-journal-entry-uuid])

(defrecord Rolon [rolon-uuid get-rolon-values])

(defrecord Rolon-value [journal-entry-uuid rolon-uuid
                        get-property-values get-property-journal-entry-uuids])

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark uuid]
  ((:get-rolon ark) ark uuid))

(defn get-journal-entries
  "returns a sorted map of all the journal entry rolons"
  [ark]
  ((:get-journal-entries ark) ark))

(defn get-other-rolons
  "returns a map of all the other rolons"
  [ark]
  ((:get-other-rolons ark) ark))

(defn create-rolon
  "returns a revised ark with the new rolon"
  [ark rolon-uuid property-values]
  ((:create-rolon ark) ark rolon-uuid property-values))

(defn destroy-rolon
  "deletes all the classifiers of a rolon,
  returning an updated ark"
  [ark rolon-uuid]
  ((:destroy-rolon ark) ark rolon-uuid))

(defn update-property
  "update the value of a property of a rolon,
  returning an updated ark"
  [ark rolon-uuid property-name property-value]
  ((:update-property ark) ark rolon-uuid property-name property-value))

(defn get-latest-journal-entry-uuid
  [ark]
  ((:get-latest-journal-entry-uuid ark) ark))

(defn get-rolon-uuid
  "returns the uuid of the rolon,
  where rec is a rolon or rolon-value"
  [rec]
  (:rolon-uuid rec))

(defn get-rolon-values
  "returns a sorted map of all the values of a rolon"
  [rolon]
  ((:get-rolon-values rolon) rolon))

(defn get-journal-entry-uuid
  "returns the type-1 uuid of the journal entry rolon which created this rolon value"
  [rolon-value]
  (:journal-entry-uuid rolon-value) rolon-value)

(defn get-property-values
  "returns the values of the properties, nil indicating the property is no longer present"
  [rolon-value]
  ((:get-property-values rolon-value) rolon-value))

(defn get-property-journal-entry-uuids
  "returns the type 1 uuid of the journal entry rolons which changed each property"
  [rolon-value]
  ((get-property-journal-entry-uuids rolon-value) rolon-value))

(defn get-latest-rolon-value
  "returns the latest rolon value"
  [rolon]
  (val (last (get-rolon-values rolon))))

(defn get-updated-rolon-uuids
  "returns a map of the uuids of the rolons updated by a journal-entry rolon"
  [journal-entry]
  (let [latest-rolon-value (get-latest-rolon-value journal-entry)
        updated-rolon-uuids (:descriptor:updated-rolon-uuids (get-property-values latest-rolon-value))]
    (if (nil? updated-rolon-uuids)
      (sorted-map)
      updated-rolon-uuids)))

(defn get-previous-value
  "returns the previous rolon value for the same rolon, or nil"
  [ark rolon-value]
  (let [journal-entry-uuid (get-journal-entry-uuid rolon-value)
        rolon (get-rolon ark (get-rolon-uuid rolon-value))
        rolon-values (get-rolon-values rolon)
        previous-rolon-values (rsubseq rolon-values < journal-entry-uuid)]
    (val (first previous-rolon-values))))

(defn get-index
  "returns a sorted map of lists of rolon uuids keyed by classifier value"
  [index-rolon]
  (let [latest-rolon-value (get-latest-rolon-value index-rolon)
        index (:descriptor:index (get-property-values latest-rolon-value))]
    (if (nil? index)
      (sorted-map)
      index)))
