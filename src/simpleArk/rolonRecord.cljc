(ns simpleArk.rolonRecord
  (:require [simpleArk.reader :as reader]))

#?(:clj
   (set! *warn-on-reflection* true))

(defrecord Rolon-record [rolon-uuid])

(defn loadRolon [m]
  (into (->Rolon-record (:rolon-uuid m)) (:changes-by-property m)))

(defn register
  [component-map]
  (reader/register-tag-parser! component-map 'simpleArk.rolonRecord.Rolon-record loadRolon))
