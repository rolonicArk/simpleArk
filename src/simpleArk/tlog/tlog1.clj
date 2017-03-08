(ns simpleArk.tlog.tlog1
  (:require [simpleArk.log.log :as log]
            [simpleArk.pub.pub :as pub]))

(set! *warn-on-reflection* true)

(defn add-tran!
  [m user-uuid capability je-uuid transaction-name s rsp-chan ark]
  (swap! (::va m) conj [user-uuid capability je-uuid transaction-name s])
  (log/info! m :transaction transaction-name s)
  (pub/publish! m ark [[rsp-chan user-uuid capability je-uuid]]))

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
