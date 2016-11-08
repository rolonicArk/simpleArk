(ns simpleArk.uuidi
  (:require [simpleArk.uuid0 :as uuid0]
            [clj-uuid :refer [v4]]))

(set! *warn-on-reflection* true)

(defn random-uuid
  [m]
  (v4 0 (swap! (::counter-atom m) inc)))

(defn- build
  [m]
  (-> m
      (assoc :uuid/random-uuid random-uuid)
      (assoc ::counter-atom (atom 0))))

(defn builder
  []
  (comp
    build
    (uuid0/builder)))
