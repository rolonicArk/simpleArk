(ns simpleArk.uuid.uuid0
  (:require [simpleArk.mapish :as mapish]
            [clj-uuid :refer [v1 v4 v5 +null+]]))

(set! *warn-on-reflection* true)

(defn journal-entry-uuid
  [_]
  (v1))

(defn random-uuid
  [_]
  (v4))

(defn index-uuid
  [_ index]
  (if (not (mapish/index? index))
    (throw (Exception. (str index " is not an index keyword"))))
  (v5 +null+ (name index)))

(defn- build
  [m]
  (-> m
      (assoc :uuid/journal-entry-uuid journal-entry-uuid)
      (assoc :uuid/random-uuid random-uuid)
      (assoc :uuid/index-uuid index-uuid)))

(defn builder
  []
  build)
