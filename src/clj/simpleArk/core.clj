(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defprotocol ark
  (get-rolon [this uuid]
    "returns the rolon identified by the uuid, or nil")
  (get-journal-entry-uuids [this]
    "returns a sorted set of all the journal entry rolon uuids")
  (get-rolon-uuids [this]
    "returns a sorted set of all rolon uuids"))

(defprotocol rolon
  (get-rolon-uuid [this]
    "returns the uuid of the rolon")
  (get-journal-entry-uuids-for-rolon [this]
    "returns a sorted set of the uuids of all the journal entries which have updated this rolon"))

(defprotocol journal-entry-rolon
  (get-updated-rolon-values [this]
    "returns a sorted set of the rolon values created by this journal entry rolon"))

(defprotocol rolon-value
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
