(ns simpleArk.pub.pub0
  (:require [clojure.core.async :as async]
            [simpleArk.arkDb.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn publish!
  [ark-db ark-value v]
  (ark-db/init-ark-db! ark-db ark-value)
  (reduce (fn [_ [chan user-uuid capability je-uuid]]
            (async/>!! chan [user-uuid capability je-uuid])
            nil)
          nil v))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::va (atom []))
        (assoc :pub/publish! publish!))))
