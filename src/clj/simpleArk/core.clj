(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defrecord Ark [get-rolon get-journal-entry-uuids])

(defrecord Rolon [get-journal-entry-uuids get-rolon-uuids get-rolon-uuid])

(defrecord Journal-entry [get-journal-entry-uuids get-rolon-uuids get-rolon-uuid
                          get-rolon-values])

(defrecord Rolon-value [get-rolon-uuid get-journal-entry-uuid get-previous-value
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
  "returns the uuid of the rolon,
  where rec is a rolon or rolon-value"
  [rec]
  (:rolon-uuid rec))

(defn get-rolon-values
  "returns a set of the rolon values created by this journal-entry rolon"
  [journal-entry]
  ((:get-rolon-values journal-entry)))

(defn get-journal-entry-uuid
  "returns the type-1 uuid of the journal entry rolon which created this rolon value"
  [rolon-value]
  (:journal-entry-uuid rolon-value))

(defn get-previous-value
  [rolon-value]
  "returns the previous rolon value for the same rolon, or nil"
  ((:get-previous-value rolon-value)))

(defn get-property-keys
  [rolon-value]
  "returns a sorted set of the keys of all the properties assigned to this or a previous rolon value"
  ((:get-property-keys rolon-value)))

(defn get-property-value
  [rolon-value]
  "returns the value of a property, or nil"
  ((:get-property-value rolon-value)))

(defn get-property-journal-entry-uuid
  [rolon-value]
  "returns the type 1 uuid of the journal entry rolon which changed the property to the given value"
  ((:get-property-journal-entry-uuid rolon-value)))

(defprotocol Ark-db
  (get-ark [this]
    "returns the current value of the ark")
  (register-transaction! [this transaction-name p]
    "defines a transaction,
    where f takes an ark and a map of string properties")
  (process-transaction [this transaction-name p]
    "process a transaction with map of string properties"))
