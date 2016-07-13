(ns simpleArk.uuid
  (require [clj-uuid :refer [get-version]]))

(defn journal-entry-uuid
  [ark-db]
  ((:uuid/journal-entry-uuid ark-db) ark-db))

(defn random-uuid
  [ark-db]
  ((:uuid/random-uuid ark-db) ark-db))

(defn index-uuid
  [ark-db classifier]
  ((:uuid/index-uuid ark-db) ark-db classifier))

(defn journal-entry-uuid?
  [uuid]
  (and (uuid? uuid)
       (= (get-version uuid) 1)))

(defn random-uuid?
  [uuid]
  (and (uuid? uuid)
       (= (get-version uuid) 4)))

(defn index-uuid?
  [uuid]
  (and (uuid? uuid)
       (= (get-version uuid) 5)))
