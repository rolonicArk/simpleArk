(ns simpleArk.arkRecord
  (:require #?(:clj [simpleArk.reader :as reader]
               :cljs [cljs.reader :as reader])))

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
