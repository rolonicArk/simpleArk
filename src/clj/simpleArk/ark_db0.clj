(ns simpleArk.ark-db0
  (:require [simpleArk.core :as ark]
            [simpleArk.log :as log]
            [simpleArk.uuid :as uuid]))

(set! *warn-on-reflection* true)

(defn open-ark
  [ark-db]
  (reset! (::ark-atom ark-db) (ark/create-ark ark-db)))

(defn get-ark
  [ark-db]
  @(::ark-atom ark-db))
