(ns simpleArk.tlog0)

(set! *warn-on-reflection* true)

(defn add-tran
  [m je-uuid transaction-name s]
  (swap! (::va m) conj [je-uuid transaction-name s]))

(defn tran-seq
  [m position]
  (seq (subvec @(::va m) position))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::va (atom []))
        (assoc :tran-logger/add-tran add-tran)
        (assoc :tran-logger/tran-seq tran-seq))))
