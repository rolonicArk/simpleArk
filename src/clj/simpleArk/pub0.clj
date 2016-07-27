(ns simpleArk.pub0
  (:require [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(defn init-ark!
  [ark-db ark]
  (reset! (::ark-atom ark-db) ark))

(defn get-ark
  [ark-db]
  @(::ark-atom ark-db))

(defn publish
  [ark-db ark v]
  (reset! (::ark-atom ark-db) ark)
  (reduce (fn [_ [chan je-uuid]]
            (async/>!! chan je-uuid)
            nil)
          nil v))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::va (atom []))
        (assoc ::ark-atom (atom nil))
        (assoc :ark-db/init-ark! init-ark!)
        (assoc :ark-db/get-ark get-ark)
        (assoc :pub/publish publish))))
