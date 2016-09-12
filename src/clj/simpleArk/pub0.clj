(ns simpleArk.pub0
  (:require [clojure.core.async :as async]
            [simpleArk.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn publish
  [ark-db ark v]
  (ark-db/init-ark! ark-db ark)
  (reduce (fn [_ [chan je-uuid]]
            (async/>!! chan je-uuid)
            nil)
          nil v))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::va (atom []))
        (assoc :pub/publish publish))))
