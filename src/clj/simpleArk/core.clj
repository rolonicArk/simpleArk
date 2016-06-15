(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defprotocol Ark-db
  (get-ark [this]
    "returns the current value of the ark")
  (register-transaction! [this transaction-name p]
    "defines a transaction,
    where f takes an ark and a map of string properties")
  (process-transaction [this transaction-name p]
    "process a transaction with map of string properties"))

(defrecord Ark [get-rolon get-journal-entry-uuids get-rolon-uuids])

(defrecord Rolon [rolon-uuid get-rolon-values])

(defrecord Rolon-value [rolon journal-entry-uuid
                        get-property-keys get-property-value get-property-journal-entry-uuid])

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark uuid]
  ((:get-rolon ark) uuid))

(defn get-journal-entry-uuids
  "returns a sorted set of all the journal entry rolon uuids"
  [ark]
  ((:get-journal-entry-uuids ark)))

(defn get-rolon-uuids
  "returns a sorted set of all rolon uuids"
  [ark]
  ((:get-rolon-uuids ark)))

(defn get-rolon-uuid
  "returns the uuid of the rolon"
  [rolon]
  (:rolon-uuid rolon))

(defn get-rolon-values
  "returns a map of the rolon values keyed by journal entry uuid"
  [rolon]
  ((:get-rolon-values rolon)))

(defn get-journal-entry-uuid
  "returns the type-1 uuid of the journal entry rolon which created this rolon value"
  [rolon-value]
  (:journal-entry-uuid rolon-value))

(defn get-property-keys
  "returns a sorted set of the keys of all the properties assigned to this or a previous rolon value"
  [rolon-value]
  ((:get-property-keys rolon-value)))

(defn get-property-value
  "returns the value of a property, or nil"
  [rolon-value property-name]
  ((:get-property-value rolon-value property-name)))

(defn get-property-journal-entry-uuid
  "returns the type 1 uuid of the journal entry rolon which changed the property to the given value"
  [rolon-value]
  ((:get-property-journal-entry-uuid rolon-value)))

(defn get-updated-rolon-uuids
  "returns the uuids of the rolons updated by a journal-entry rolon"
  [journal-entry]
  (let [current-value (last (get-rolon-values journal-entry))]
    (get-property-value current-value :journal-entry:updated-rolon-uuids)))

(defn get-value-rolon
  "returns the rolon"
  [rolon-value]
  (:rolon rolon-value))

(defn get-previous-value
  "returns the previous rolon value for the same rolon, or nil"
  [rolon-value]
  (let [rolon (get-value-rolon rolon-value)
        rolon-values (get-rolon-values rolon)
        journal-entry-uuid (get-journal-entry-uuid rolon-value)
        previous-values (rsubseq rolon-values < journal-entry-uuid)]
    (val (first previous-values))))
