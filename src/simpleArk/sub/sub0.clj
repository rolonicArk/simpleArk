(ns simpleArk.sub.sub0)

(set! *warn-on-reflection* true)

(defn subscribe!
  "register a function to receive a stream of je-uuids"
  [ark-db id f]
  (swap! (::sa ark-db) assoc id f))

(defn notify!
  "send a je-uuid to all subscribers"
  [ark-db je-uuid]
  (reduce
    (fn [_ se]
      ((val se) je-uuid))
    nil
    @(::sa ark-db)))

(defn builder
  []
  (fn [m]
    (-> m
        (assoc ::sa (atom {}))
        (assoc :sub/subscribe subscribe!)
        (assoc :sub/notify notify!)
        )))
