(ns simpleArk.ark-db)

(set! *warn-on-reflection* true)

(defn open-ark!
  "Open the ark after ark-db is finalized."
  [ark-db]
  ((:ark-db/open-ark! ark-db) ark-db))

(defn async-process-transaction!
  [ark-db transaction-name s rsp-chan]
  ((:ark-db/async-process-transaction! ark-db) ark-db transaction-name s rsp-chan))

(defn process-transaction!
  "process a transaction with an (edn) string,
    returning the new journal-entry uuid"
  ([ark-db transaction-name s]
   ((:ark-db/process-transaction! ark-db) ark-db transaction-name s))
  ([ark-db je-uuid transaction-name s]
   ((:ark-db/process-transaction-at! ark-db) ark-db je-uuid transaction-name s)))

(defn update-ark-db
  "applies a transaction to the ark-atom"
  [ark-db je-uuid transaction-name s]
  (swap! (::ark-atom ark-db)
         (fn [ark-value]
           ((:ark-value/update-ark ark-db) ark-value ark-db je-uuid transaction-name s))))

(defn init-ark-db!
  "initializes the ark-atom with the value of the ark."
  [ark-db ark-value]
  (reset! (::ark-atom ark-db) ark-value))

(defn get-ark-value
  "returns the value of the ark"
  [ark-db]
  @(::ark-atom ark-db))

(defn builder
  []
  (fn [m]
    (assoc m ::ark-atom (atom nil))))
