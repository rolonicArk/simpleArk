(ns simpleArk.arkDb.ark-dba1
  (:require [simpleArk.arkValue.ark-value :as ark-value]
            [simpleArk.log.log :as log]
            [simpleArk.tlog.tlog :as tlog]
            [simpleArk.uuid.uuid :as uuid]
            [simpleArk.arkDb.ark-db :as ark-db]
            [clojure.core.async :as async]
            [simpleArk.closer :as closer]))

(set! *warn-on-reflection* true)

(defn- close-tran-chan
  [ark-db]
  (let [tran-chan (::tran-chan ark-db)]
    (async/close! tran-chan)
    (log/info! ark-db "transaction channel closed")))

(defn- process-transactions
  [ark-db]
  (let [tran (async/<!! (::tran-chan ark-db))]
    (when tran
      (let [[user-uuid capability transaction-name s rsp-chan] tran
            je-uuid (uuid/journal-entry-uuid ark-db)]
        (try
          (ark-db/update-ark-db ark-db user-uuid capability je-uuid transaction-name s)
          (tlog/add-tran! ark-db user-uuid capability je-uuid transaction-name s rsp-chan
                          (ark-db/get-ark-record ark-db))
          (catch Exception e
            (log/warn! ark-db "transaction failure" user-uuid capability transaction-name s
                       (.toString e))
            (async/>!! rsp-chan e))))
      (recur ark-db))))

(defn open-ark!
  [ark-db]
  (ark-db/update-ark-db! ark-db (ark-value/create-ark ark-db))
  (async/thread (process-transactions ark-db))
  (closer/open-component ark-db (::name ark-db) close-tran-chan)
  )

(defn async-process-transaction!
  [ark-db user-uuid capability transaction-name s rsp-chan]
  (async/>!! (::tran-chan ark-db) [user-uuid capability transaction-name s rsp-chan]))

(defn process-transaction!
  ([ark-db user-uuid capability transaction-name s]
   (let [rsp-chan (async/chan)
         _ (async-process-transaction! ark-db user-uuid capability transaction-name s rsp-chan)
         rsp (async/<!! rsp-chan)]
     (if (instance? Exception rsp)
       (throw rsp)
       rsp)))
  ([ark-db user-uuid capability je-uuid transaction-name s]
   (ark-db/update-ark-db ark-db user-uuid capability je-uuid transaction-name s)
   (log/info! ark-db :transaction user-uuid capability transaction-name s)
   je-uuid))

(defn builder
  [& {:keys [tran-chan name]
      :or {tran-chan (async/chan 100)
           name "ark-dba1"}}]
  (fn [m]
    (-> m
        (assoc ::tran-chan tran-chan)
        (assoc ::name name)
        (assoc :ark-db/open-ark! open-ark!)
        (assoc :ark-db/async-process-transaction! async-process-transaction!)
        (assoc :ark-db/process-transaction! process-transaction!))))
