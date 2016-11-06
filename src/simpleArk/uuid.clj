(ns simpleArk.uuid
  (require [clj-uuid :refer [get-version]]))

(set! *warn-on-reflection* true)

(defn journal-entry-uuid
  [ark-db]
  ((:uuid/journal-entry-uuid ark-db) ark-db))

(defn random-uuid
  [ark-db]
  ((:uuid/random-uuid ark-db) ark-db))

(defn index-uuid
  [ark-db index]
  ((:uuid/index-uuid ark-db) ark-db index))

(defn lsw
  [uuid]
  (long (+ 9223372036854775808 (clj-uuid/get-word-low uuid))))

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
