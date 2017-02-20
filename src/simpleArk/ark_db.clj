(ns simpleArk.ark-db
  (:require [simpleArk.arkRecord :as arkRecord]
            [simpleArk.mapish :as mapish]
            [simpleArk.log :as log]))

(set! *warn-on-reflection* true)

(defn open-ark!
  "Open the ark after ark-db is finalized."
  [ark-db]
  ((:ark-db/open-ark! ark-db) ark-db))

(defn async-process-transaction!
  [ark-db user-uuid transaction-name s rsp-chan]
  ((:ark-db/async-process-transaction! ark-db) ark-db user-uuid transaction-name s rsp-chan))

(defn process-transaction!
  "process a transaction with an (edn) string,
    returning the new journal-entry uuid"
  ([ark-db transaction-name s]
   ((:ark-db/process-transaction! ark-db) ark-db nil nil transaction-name s))
  ([ark-db user-uuid capability transaction-name s]
   ((:ark-db/process-transaction! ark-db) ark-db user-uuid capability transaction-name s))
  ([ark-db user-uuid capability je-uuid transaction-name s]
   ((:ark-db/process-transaction! ark-db) ark-db user-uuid capability je-uuid transaction-name s)))

(defn update-ark-db
  "applies a transaction to the ark-atom"
  [ark-db user-uuid capability je-uuid transaction-name s]
  (swap! (::ark-atom ark-db)
         (fn [ark-value]
           ((:ark-value/update-ark ark-db) ark-value ark-db user-uuid je-uuid transaction-name s))))

(defmulti notification
          (fn [label je-uuid watcher modified]
            label))

(defmethod notification :default
  [label je-uuid watcher modified]
  (println :default-notification label je-uuid watcher modified))

(declare get-ark-record)

(defn process-notifications
  [ark-db je-uuid]
  (let [ark-record (get-ark-record ark-db)
        properties (arkRecord/get-property-values ark-record je-uuid)]
    (reduce
      (fn [_ e]
        (let [modified (nth (key e) 1)
              p (arkRecord/get-property-values ark-record modified)
              ir (mapish/mi-sub p [:inv-rel/watches])]
          (reduce
            (fn [_ e]
              (let [path (key e)
                    label (nth path 1)
                    watcher (nth path 2)]
                (try
                  (notification label je-uuid watcher modified)
                  (catch Exception e
                    (log/error! (str e)))
                  )
                nil))
            nil
            ir)))
      nil
      (mapish/mi-sub properties [:rel/modified]))))

(defn init-ark-db!
  "initializes the ark-atom with the value of the ark."
  [ark-db ark-value]
  (reset! (::ark-atom ark-db) ark-value))

(defn get-ark-record
  "returns the value of the ark"
  [ark-db]
  @(::ark-atom ark-db))

(defn builder
  []
  (fn [m]
    (assoc m ::ark-atom (atom nil))))
