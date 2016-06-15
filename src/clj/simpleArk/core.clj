(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defrecord ark [])

(defrecord rolon [])

(defrecord journal-entry [])

(defrecord rolon-value [])

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark uuid]
  ((::get-rolon ark) uuid))

(defn get-journal-entry-uuids
  "returns a sorted set of all the journal entry rolon uuids"
  [ark]
  ((::get-journal-entry-uuids ark)))

(defn get-rolon-uuids
  "returns a sorted set of all rolon uuids"
  [ark]
  ((::get-rolon-uuids ark)))

(defprotocol rolon-protocol
  (get-rolon-uuid [this]
    "returns the uuid of the rolon")
  (get-journal-entry-uuids-for-rolon [this]
    "returns a sorted set of the uuids of all the journal entries which have updated this rolon"))

(defprotocol journal-entry-rolon-protocol
  (get-updated-rolon-values [this]
    "returns a sorted set of the rolon values created by this journal entry rolon"))

(defprotocol rolon-value-protocol
  (get-rolon-uuid-for-value [this]
    "returns the uuid of the rolon")
  (get-journal-entry-uuid [this]
    "returns the type-1 uuid of the journal entry rolon which created this rolon value")
  (get-previous-value [this]
    "returns the previous rolon value for the same rolon, or nil")
  (get-property-keys [this]
    "returns a sorted set of the keys of all the properties assigned to this or a previous rolon vale")
  (get-property-value [this property-key]
    "returns the value of a property, or nil")
  (get-property-journal-entry-uuid [this]
    "returns the type 1 uuid of the journal entry rolon which changed the property to the given value"))
