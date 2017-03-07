(ns simpleArk.tlog0
  (:require [clojure.core.async :as async]
            [simpleArk.log :as log]
            [simpleArk.arkDb.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn add-tran!
  [ark-db je-uuid transaction-name s rsp-chan ark]
  (swap! (::va ark-db) conj [je-uuid transaction-name s])
  (ark-db/init-ark-db! ark-db ark)
  (log/info! ark-db :transaction transaction-name s)
  (async/>!! rsp-chan je-uuid))

(defn tran-seq
  [ark-db position]
  (let [vi (volatile! position)]
    (map (fn [v]
           (conj v (vswap! vi inc)))
         (subvec @(::va ark-db) position))))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::va (atom []))
        (assoc :tran-logger/add-tran add-tran!)
        (assoc :tran-logger/tran-seq tran-seq))))
