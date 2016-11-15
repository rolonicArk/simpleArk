(ns simpleArk.rolonRecord)

#?(:clj
   (set! *warn-on-reflection* true))

(defrecord Rolon-record [rolon-uuid])

(defn loadRolon [m]
  (into (->Rolon-record (:rolon-uuid m)) (:changes-by-property m)))
