(ns simpleArk.pub)

(defn publish
  "publish the updated ark and returns the je-uuids"
  [ark-db ark v]
  ((:pub/publish ark-db) ark-db ark v))
