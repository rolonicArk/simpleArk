(ns simpleArk.ark-db)

(set! *warn-on-reflection* true)

(defn open-ark!
  [ark-db]
  "Open the ark after ark-db is finalized."
  ((:ark-db/open-ark! ark-db) ark-db))

(defn process-transaction!
  "process a transaction with an (edn) string,
    returning the new journal-entry uuid"
  ([ark-db transaction-name s]
   ((:ark-db/process-transaction! ark-db) ark-db transaction-name s))
  ([ark-db je-uuid transaction-name s]
   ((:ark-db/process-transaction-at! ark-db) ark-db je-uuid transaction-name s)))

(defn get-ark-atom
  [this]
  (::ark-atom this))

(defn init-ark!
  [ark-db ark]
  (reset! (get-ark-atom ark-db) ark))

(defn get-ark
  [ark-db]
  @(get-ark-atom ark-db))

(defn- build
  "returns the ark db common data"
  [m]
  (assoc m ::ark-atom (atom nil)))

(defn builder
  []
  build)
