(ns simpleArk.actions
  (:require [simpleArk.mapish :as mapish]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defmulti
  action
  (fn [state ark-db v]
    (let [kw (first v)]
      (cond
        (mapish/index? kw) :property
        (mapish/content? kw) :property
        (mapish/rel? kw) :relation
        (mapish/inv-rel? kw) :relation
        (mapish/bi-rel? kw) :relation
        :else kw))))

(defn eval-actions
  [state ark-db actions]
  (reduce
    (fn [state v]
      (action state ark-db v))
    state
    actions))

(defmethod ark-value/eval-transaction :actions-transaction!
  [ark-record ark-db n s]
  (let [[local actions] (read-string s)]
    (second (eval-actions [local ark-record] ark-db actions))))

(defn process-actions!
  [ark-db local actions]
  (let [s (pr-str [local actions])]
    (ark-db/process-transaction! ark-db :actions-transaction! s)))

(defmethod action :property
  [[local ark-record] ark-db [kw rolon-uuid path value]]
  (let [rolon-uuid
        (if (= :je rolon-uuid)
          (arkRecord/get-latest-journal-entry-uuid ark-record)
          rolon-uuid)
        ark-record
        (if (arkRecord/get-rolon ark-record rolon-uuid)
          ark-record
          (ark-value/assoc-rolon
            ark-record
            rolon-uuid
            (rolonRecord/->Rolon-record rolon-uuid)))
        ark-record (ark-value/update-property ark-record ark-db rolon-uuid path value)]
    [local ark-record]))

(defn build-property
  [actions rolon-uuid path value]
  (conj actions [(first path) rolon-uuid path value]))

(defn build-je-property
  [actions path value]
  (conj actions [(first path) :je path value]))

(defmethod action :println
  [[local ark-record] ark-db [kw s]]
  (println s)
  [local ark-record])

(defn build-println
  [actions s]
  (conj actions [:println s]))
