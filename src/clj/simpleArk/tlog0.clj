(ns simpleArk.tlog0
  (:require [clojure.core.async :as async]
            [simpleArk.log :as log]
            [simpleArk.ark-db :as ark-db]))

(set! *warn-on-reflection* true)

(defn add-tran!
  [m je-uuid transaction-name s rsp-chan ark]
  (swap! (::va m) conj [je-uuid transaction-name s])
  (reset! (ark-db/get-ark-atom m) ark)
  (log/info! m :transaction transaction-name s)
  (async/>!! rsp-chan je-uuid))

(defn tran-seq
  [m position]
  (def vi (volatile! position))
  (map (fn [v]
         (conj v (vswap! vi inc)))
       (subvec @(::va m) position)))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::va (atom []))
        (assoc :tran-logger/add-tran add-tran!)
        (assoc :tran-logger/tran-seq tran-seq))))
