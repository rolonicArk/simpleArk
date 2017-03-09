(ns simpleArk.pub.pub0
  (:require [clojure.core.async :as async]
            [simpleArk.arkDb.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn publish!
  [ark-db arkRecord v]
  (ark-db/update-ark-db! ark-db arkRecord)
  (reduce (fn [_ [chan je-uuid]]
            (async/>!! chan je-uuid)
            nil)
          nil v))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc :pub/publish! publish!))))
