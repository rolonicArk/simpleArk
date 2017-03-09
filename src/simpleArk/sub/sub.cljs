(ns simpleArk.sub.sub)

(set! *warn-on-reflection* true)

(defn subscribe!
  "register a function to receive a stream of je-uuids"
  [ark-db f]
  (:sub/subscribe ark-db) ark-db f)

(defn notify!
  "send a je-uuid to all subscribers"
  [ark-db je-uuid]
  (:sub/notify ark-db) ark-db je-uuid)
