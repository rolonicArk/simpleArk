(ns simpleArk.impl0
  (:require [clj-uuid :as uuid]
            [simpleArk.core :as ark]))

(defn get-rolon-values
  [rolon]
  (::rolon-values rolon))

(defn create-rolon
  [rolon-uuid]
  (let [rolon (ark/->Rolon rolon-uuid get-rolon-values)
        rolon (assoc rolon ::rolon-values (sorted-map))]
    rolon))

(defn create-ark
  []
  (ark/->Ark nil nil create-rolon nil nil))

(defrecord Db [ark-atom registry-atom]
  ark/Ark-db
  (get-ark [this]
    @ark-atom)
  (register-transaction! [this transaction-name f]
    (swap! registry-atom #(assoc % transaction-name f)))
  (process-transaction! [this transaction-name s]
    (let [je-uuid (uuid/v1)
          je (create-rolon je-uuid)]
      (swap! ark-atom (@registry-atom transaction-name) je s))))

(defn create-ark-db
  "returns an ark db"
  []
  (let [ark (create-ark)
        ark-atom (atom ark)
        registry-atom (atom (sorted-map))
        ark-db (->Db ark-atom registry-atom)]
    ark-db))
