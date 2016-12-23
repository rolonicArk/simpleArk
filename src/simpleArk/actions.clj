(ns simpleArk.actions
  (:require [simpleArk.mapish :as mapish]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]))

(defmulti
  action
  (fn [local ark-record ark-db v]
    (let [kw (first v)]
      (cond
        (mapish/index? kw) :property
        (mapish/content? kw) :property
        (mapish/rel? kw) :relation
        (mapish/inv-rel? kw) :relation
        (mapish/bi-rel? kw) :relation
        :else kw))))

(defn action-eval
  [local ark-record ark-db actions]
  (if (nil? actions)
    [local ark-record]
    (let [[local ark-record] (action ark-record (first v))]
      (recur local ark-record (next actions)))))

(defmethod update-property-action
  :property
  [local ark-record ark-db [kw rolon-uuid path value]]
  (let [ark-record
        (if (arkRecord/get-rolon ark-record rolon-uuid)
          ark-record
          (ark-value/assoc-rolon
            ark-record
            rolon-uuid
            (rolonRecord/->Rolon-record rolon-uuid)))
        ark-record (ark-value/update-property ark-record ark-db rolon-uuid path value)]
    [local ark-record]))

(defn update-property
  [local ark-record ark-db rolon-uuid path value]
  (action-eval local ark-record ark-db [(first path) rolon-uuid path value]))


