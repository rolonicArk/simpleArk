(ns simpleArk.reg0
  (:require [simpleArk.core :as ark]))

(set! *warn-on-reflection* true)

(defn register-transaction!
  [reg transaction-name f]
  (swap! (::registry-atom reg) #(assoc % transaction-name f)))

(defn get-transaction
  [reg transaction-name]
  (transaction-name @(::registry-atom reg)))

(defn- build
  "returns an ark db"
  [m]
  (let [registry-atom (atom (sorted-map))
        reg (-> m
                (assoc ::registry-atom registry-atom)
                (assoc :reg/register-transaction! register-transaction!)
                (assoc :reg/get-transaction get-transaction)
                )]
    reg))

(defn builder
  []
  build)
