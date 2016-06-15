(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defrecord ark [])

(defrecord rolon [])

(defrecord journal-entry [])

(defrecord rolon-value [])

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark uuid]
  ((:ark:get-rolon ark) uuid))

(defn get-journal-entry-uuids
  "returns a sorted set of all the journal entry rolon uuids,
  where rec is an ark or a rolon"
  [rec]
  ((:ark:get-journal-entry-uuids rec)))

(defn get-rolon-uuids
  "returns a sorted set of all rolon uuids"
  [ark]
  ((:ark:get-rolon-uuids ark)))

(defn get-rolon-uuid
  "returns the uuid of the rolon,
  where rec is a rolon or rolon-value"
  [rec]
  (:ark:rolon-uuid rec))

(defn get-rolon-values
  "returns a set of the rolon values created by this journal-entry rolon"
  [journal-entry]
  ((:ark:get-rolon-values journal-entry)))

(defprotocol rolon-value-protocol
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
