(ns simpleArk.pub.pub)

(set! *warn-on-reflection* true)

(defn publish!
  "publishs the updated ark value and returns the je-uuids,
  where v is [response-channel user-uuid capability je-uuid]"
  [ark-db arkRecord v]
  ((:pub/publish! ark-db) ark-db arkRecord v))
