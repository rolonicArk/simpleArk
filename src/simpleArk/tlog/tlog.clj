(ns simpleArk.tlog.tlog)

(defn add-tran!
  "appends to the transaction logger and returns the position.
  Publishes the updated ark and
  asynchronously replys with the je-uuid once a transaction is flushed"
  [m user-uuid capability je-uuid transaction-name s rsp-chan ark]
  ((:tran-logger/add-tran m) m user-uuid capability je-uuid transaction-name s rsp-chan ark))

(defn tran-seq
  "returns a sequence of transactions with the position of the next transaction,
  e.g. [je-uuid, transaction-name, s, position].
  The optional position parameter allows you to resume the sequence at any point"
  ([m] (tran-seq m 0))
  ([m position]
   ((:tran-logger/tran-seq m) m position)))
