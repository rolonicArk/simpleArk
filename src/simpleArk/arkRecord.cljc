(ns simpleArk.arkRecord
  (:require #?(:clj
                [simpleArk.reader :as reader]
               :cljs
               [cljs.reader :as reader])
                [simpleArk.mapish :as mapish]
                [simpleArk.uuid :as uuid]))

#?(:clj
   (set! *warn-on-reflection* true))

(defrecord Ark-record [])

(defn load-ark [m]
  (into (->Ark-record) m))

#?(:clj
   (defn register
     [component-map]
     (reader/register-tag-parser! component-map 'simpleArk.arkRecord.Ark-record load-ark))
   :cljs
   (reader/register-tag-parser! "simpleArk.arkRecord.Ark-record" load-ark))

(defn get-selected-time [ark-record]
  (:selected-time ark-record))

(defn get-latest-journal-entry-uuid
  [ark-record]
  (:latest-journal-entry-uuid ark-record))

(defn get-journal-entries
  [ark-record]
  (mapish/mi-sub (:journal-entries ark-record) nil nil <= (get-selected-time ark-record)))

(defn get-indexes
  [ark-record]
  (:indexes ark-record))

(defn get-random-rolons
  [ark-record]
  (:random-rolons ark-record))

(defn select-time
  [ark-record je-uuid]
  (let [jes
        (mapish/mi-sub
          (get-journal-entries ark-record)
          nil
          nil
          <=
          [je-uuid])
        je-uuid
        (key
          (first
            (rseq jes)))]
    (assoc ark-record :selected-time je-uuid)))

(defn get-rolon
  [ark-record uuid]
  (cond
    (uuid/journal-entry-uuid? uuid) (get (get-journal-entries ark-record) [uuid])
    (uuid/index-uuid? uuid) (get (get-indexes ark-record) [uuid])
    (uuid/random-uuid? uuid) (get (get-random-rolons ark-record) [uuid])
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn get-changes-by-property
  ([ark-record rolon-uuid property-path]
   (let [changes-by-property (get-changes-by-property ark-record rolon-uuid)
         pc (get changes-by-property property-path)]
     (if (nil? pc)
       nil
       (mapish/mi-sub pc nil nil <= (get-selected-time ark-record)))))
  ([ark-value rolon-uuid]
   (:changes-by-property (get-rolon ark-value rolon-uuid))))
