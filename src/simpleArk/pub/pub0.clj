(ns simpleArk.pub.pub0
  (:require [clojure.core.async :as async]
            [simpleArk.sub.sub :as sub]
            [simpleArk.arkDb.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn publish!
  [ark-db arkRecord [chan user-uuid capability je-uuid]]
    (if (instance? Exception je-uuid)
      (sub/notify! ark-db je-uuid)
      (do
        (ark-db/update-ark-db! ark-db arkRecord)
        (sub/notify! ark-db je-uuid)
        (println :publish je-uuid)
        ;(ark-db/process-notifications ark-db je-uuid)
        (async/>!! chan je-uuid)
        )))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc :pub/publish! publish!))))
