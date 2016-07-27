(ns simpleArk.tlog1
  (:require [clojure.core.async :as async]
            [simpleArk.log :as log]
            [simpleArk.core :as ark]))

(set! *warn-on-reflection* true)

(defn add-tran!
  [m je-uuid transaction-name s rsp-chan ark]
  (swap! (::va m) conj [je-uuid transaction-name s])
  (log/info! m :transaction transaction-name s)
  (ark/publish m ark [[rsp-chan je-uuid]]))

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
