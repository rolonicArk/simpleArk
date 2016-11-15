(ns simpleArk.arkRecord
  (:require [simpleArk.ark-value :as ark-value]))

#?(:clj
   (set! *warn-on-reflection* true))

(defrecord Ark-record [])

(defn create-mi
  [ark-db & keyvals]
  (apply (:ark-value/create-mi ark-db) ark-db keyvals))

(defn ark-value-assoc-mapish
  [ark-value ark-db key]
  (let [mi (create-mi ark-db)]
    (assoc ark-value key mi)))

(defn init-ark-value
  [ark-value ark-db]
  (-> ark-value
      (ark-value-assoc-mapish ark-db :journal-entries)
      (ark-value-assoc-mapish ark-db :indexes)
      (ark-value-assoc-mapish ark-db :random-rolons)))

(defn create-ark [ark-db]
  (-> (->Ark-record)
      (init-ark-value ark-db)))
