(ns simpleArk.tlog0
  (:require [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(defn init-ark!
  [ark-db ark]
  (reset! (::ark-atom ark-db) ark))

(defn get-ark
  [ark-db]
  @(::ark-atom ark-db))

(defn add-tran!
  [m je-uuid transaction-name s rsp-chan ark]
  (swap! (::va m) conj [je-uuid transaction-name s])
  (reset! (::ark-atom m) ark)
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
        (assoc ::ark-atom (atom nil))
        (assoc :ark-db/init-ark! init-ark!)
        (assoc :ark-db/get-ark get-ark)
        (assoc :tran-logger/add-tran add-tran!)
        (assoc :tran-logger/tran-seq tran-seq))))
