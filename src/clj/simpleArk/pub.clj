(ns simpleArk.pub)

(defn publish
  "publishs the updated ark value and returns the je-uuids,
  where v is [response-channel je-uuid]"
  [ark-db ark-value v]
  ((:pub/publish ark-db) ark-db ark-value v))
