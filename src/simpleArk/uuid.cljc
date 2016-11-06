(ns simpleArk.uuid
  (:require #?(:clj  [clj-uuid :refer [get-version]])))

#?(:clj
   (set! *warn-on-reflection* true))

#?(:cljs
   (defn get-version [uuid]
     (js/parseInt (subs (prn-str uuid)  21 22))))

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

#?(:clj
   (defn journal-entry-uuid
     [ark-db]
     ((:uuid/journal-entry-uuid ark-db) ark-db)))

#?(:clj
   (defn random-uuid
     [ark-db]
     ((:uuid/random-uuid ark-db) ark-db)))

#?(:clj
   (defn index-uuid
     [ark-db index]
     ((:uuid/index-uuid ark-db) ark-db index)))

#?(:clj
   (defn lsw
     [uuid]
     (long (+ 9223372036854775808 (clj-uuid/get-word-low uuid)))))
