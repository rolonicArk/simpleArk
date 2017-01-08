(ns simpleArk.uuid
  (:require #?(:clj [clj-uuid :refer [get-version get-instant]]))
  #?(:clj
     (:import (java.util UUID)
              (java.lang Comparable))))

#?(:clj
   (set! *warn-on-reflection* true))

#?(:cljs
   (defn get-version [uuid]
     (js/parseInt (subs (prn-str uuid) 21 22))))

#?(:clj
   (deftype Timestamp [value]
     Comparable
     (compareTo [this o]
       (let [^Timestamp o o]
         (compare value (.value o)))))
   :cljs
   (deftype Timestamp [value]
     IComparable
     (-compare [x y]
       (compare value (.value y)))))

(def x (compare 1 2))

(defn timestamp [uuid]
  (let [s (prn-str uuid)]
    #?(:clj
       (java.lang.Long/parseLong
         (str (subs s 22 25) (subs s 16 20) (subs s 7 15))
         16)
       :cljs
       (js/parseInt
         (str (subs s 22 25) (subs s 16 20) (subs s 7 15))
         16)
       )))

(defn posix-time [ts]
  (- (quot ts 10000) 12219292800000))

(defn get-time [uuid]
  (posix-time (timestamp uuid)))

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

(defn rolon-key [uuid]
  (if (journal-entry-uuid? uuid)
    (timestamp uuid)
    uuid))

#?(:clj  (defn create-uuid [s] (UUID/fromString s))
   :cljs (defn create-uuid [s] (uuid s)))

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
