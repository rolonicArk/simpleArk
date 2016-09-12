(ns simpleArk.ark-dba0
  (:require [simpleArk.core :as ark]
            [simpleArk.log :as log]
            [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [clojure.core.async :as async]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(defn close-tran-chan
  [ark-db]
  (let [tran-chan (::tran-chan ark-db)]
    (async/close! tran-chan)
    (log/info! ark-db "transaction channel closed")))

(defn process-transactions
  [ark-db]
  (let [tran (async/<!! (::tran-chan ark-db))]
    (when tran
      (let [[transaction-name s rsp-chan] tran
            je-uuid (uuid/journal-entry-uuid ark-db)]
        (try
          (ark-db/update-ark-db ark-db je-uuid transaction-name s)
          (log/info! ark-db :transaction transaction-name s)
          (async/>!! rsp-chan je-uuid)
          (catch Exception e
            (log/warn! ark-db "transaction failure" transaction-name s
                       (.toString e))
            (async/>!! rsp-chan e))))
      (recur ark-db))))

(defn open-ark!
  [ark-db]
  (ark-db/reset-ark! ark-db (ark/create-ark ark-db))
  (async/thread (process-transactions ark-db))
  (closer/open-component ark-db (::name ark-db) close-tran-chan)
  )

(defn process-transaction!
  ([ark-db transaction-name s]
   (let [rsp-chan (async/chan)
         _ (async/>!! (::tran-chan ark-db) [transaction-name s rsp-chan])
         rsp (async/<!! rsp-chan)]
     (if (instance? Exception rsp)
       (throw rsp)
       rsp)))
  ([ark-db je-uuid transaction-name s]
   (ark-db/update-ark-db ark-db je-uuid transaction-name s)
   (log/info! ark-db :transaction transaction-name s)
   je-uuid))

(defn builder
  [& {:keys [tran-chan name]
      :or {tran-chan (async/chan 100)
           name "ark-dba0"}}]
  (fn [m]
    (-> m
        (assoc ::tran-chan tran-chan)
        (assoc ::name name)
        (assoc :ark-db/open-ark! open-ark!)
        (assoc :ark-db/process-transaction! process-transaction!))))
