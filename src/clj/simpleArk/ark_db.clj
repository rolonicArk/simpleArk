(ns simpleArk.ark-db)

(set! *warn-on-reflection* true)

(defn init-ark!
  "initialize the ark"
  [m ark]
  ((:ark-db/init-ark! m) m ark))

(defn open-ark!
  [ark-db]
  "Open the ark after ark-db is finalized."
  ((:ark-db/open-ark! ark-db) ark-db))

(defn get-ark
  "returns the current value of the ark"
  [ark-db]
  ((:ark-db/get-ark ark-db) ark-db))

(defn process-transaction!
  "process a transaction with an (edn) string,
    returning the new journal-entry uuid"
  ([ark-db transaction-name s]
   ((:ark-db/process-transaction! ark-db) ark-db transaction-name s))
  ([ark-db je-uuid transaction-name s]
   ((:ark-db/process-transaction-at! ark-db) ark-db je-uuid transaction-name s)))
