(ns simpleArk.uuid
  #?(:clj
           (:require
             [clj-uuid :refer [get-version get-instant]]
             [simpleArk.reader :as reader])
     :cljs (:require
             [cljs.reader :as reader]))
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
         (compare value (.-value o))))

     Object
     (equals [a b]
       (let [b ^Timestamp b]
         (= value (.-value b))))
     (hashCode [this] (.hashCode value))
     (toString [this] (str value)))

   :cljs
   (deftype Timestamp [value]

     Object
     (toString [_] (str value))
     (equiv [this other]
       (-equiv this other))

     IHash
     (-hash [this]
       (hash value))

     IPrintWithWriter
     (-pr-writer [_ writer _]
       (-write writer (str "#uuid/Timestamp " value)))

     IEquiv
     (-equiv [_ other]
       (and (instance? Timestamp other) (identical? value (.-value other))))

     IComparable
     (-compare [x y]
       (let [^y Timestamp y]
         (compare value (.-value y))))))

(defn load-timestamp
  [v]
  (->Timestamp v))

#?(:clj
   (defn register
     [component-map]
     (reader/register-tag-parser! component-map 'uuid/Timestamp load-timestamp))
   :cljs
   (reader/register-tag-parser! "uuid/Timestamp" load-timestamp))

#?(:clj
   (defmethod print-method Timestamp [^Timestamp this ^java.io.Writer w]
     (.write w "#uuid/Timestamp ")
     (.write w (str (.-value this)))))

(defn timestamp [uuid]
  (let [s (prn-str uuid)
        l #?(:clj
             (java.lang.Long/parseLong
               (str (subs s 22 25) (subs s 16 20) (subs s 7 15))
               16)
             :cljs
             (js/parseInt
               (str (subs s 22 25) (subs s 16 20) (subs s 7 15))
               16)
             )]
    (->Timestamp l)))

(defn posix-time [ts]
  (- (quot ts 10000) 12219292800000))

(defn get-time [uuid]
  (let [^Timestamp ts (timestamp uuid)]
  (posix-time (.-value ts))))

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
