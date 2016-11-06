(ns simpleArk.uuid0
  (:require [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(defn journal-entry-uuid
  [_]
  (clj-uuid/v1))

(defn random-uuid
  [_]
  (clj-uuid/v4))

(defn index-uuid
  [_ index]
  (if (not (mapish/index? index))
    (throw (Exception. (str index " is not an index keyword"))))
  (clj-uuid/v5 clj-uuid/+null+ (name index)))

(defn- build
  [m]
  (-> m
      (assoc :uuid/journal-entry-uuid journal-entry-uuid)
      (assoc :uuid/random-uuid random-uuid)
      (assoc :uuid/index-uuid index-uuid)))

(defn builder
  []
  build)
