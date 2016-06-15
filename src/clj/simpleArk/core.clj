(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defprotocol Ark-db
  (get-ark [this]
    "returns the current value of the ark")
  (register-transaction! [this transaction-name p]
    "defines a transaction,
    where f takes an ark, a new journal-entry rolon and a map of string properties")
  (process-transaction [this transaction-name p]
    "process a transaction with map of string properties,
    returning the new journal-entry uuid"))

(defrecord Ark [get-rolon get-journal-entry-uuids get-rolon-uuids])

(defrecord Rolon [rolon-uuid get-journal-entry-uuids get-rolon-value])

(defrecord Rolon-value [value-rolon journal-entry-uuid
                        get-property-keys get-property-value get-property-journal-entry-uuid])

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark uuid]
  ((:get-rolon ark) uuid))

(defn get-journal-entry-uuids
  "returns a sorted set of all the journal entry rolon uuids,
  where rec is an ark or a rolon"
  [rec]
  ((:get-journal-entry-uuids rec)))

(defn get-rolon-uuids
  "returns a sorted set of all rolon uuids"
  [ark]
  ((:get-rolon-uuids ark)))

(defn get-rolon-uuid
  "returns the uuid of the rolon"
  [rolon]
  (:rolon-uuid rolon))

(defn get-rolon-value
  "returns the rolon value keyed by the journal entry uuid"
  [rolon journal-entry-uuid]
  ((:get-rolon-value rolon) journal-entry-uuid))

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
  ((:get-property-value rolon-value) property-name))

(defn get-property-journal-entry-uuid
  "returns the type 1 uuid of the journal entry rolon which changed the property to the given value"
  [rolon-value property-name]
  ((:get-property-journal-entry-uuid rolon-value) property-name))

(defn get-value-rolon
  "returns the rolon"
  [rolon-value]
  (:value-rolon rolon-value))

(defn get-updated-rolon-uuids
  "returns the uuids of the rolons updated by a journal-entry rolon"
  [journal-entry]
  (let [last-journal-entry-uuid (last (get-journal-entry-uuids journal-entry))
        latest-rolon-value (get-rolon-value journal-entry last-journal-entry-uuid)
        updated-rolon-uuids (get-property-value latest-rolon-value :descriptor:updated-rolon-uuids)]
    (if (nil? updated-rolon-uuids)
      #{}
      updated-rolon-uuids)))

(defn get-previous-value
  "returns the previous rolon value for the same rolon, or nil"
  [rolon-value]
  (let [journal-entry-uuid (get-journal-entry-uuid rolon-value)
        rolon (get-value-rolon rolon-value)
        journal-entry-uuids (get-journal-entry-uuids rolon)
        previous-journal-entry-uuids (rsubseq journal-entry-uuids < journal-entry-uuid)]
    (first previous-journal-entry-uuids)))
