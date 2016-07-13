(ns simpleArk.uuid0
  (:require [simpleArk.core :as ark]
            [simpleArk.uuid :as uuid]))

(defn journal-entry-uuid
  [_]
  (clj-uuid/v1))

(defn random-uuid
  [_]
  (clj-uuid/v4))

(defn index-uuid
  [_ classifier]
  (if (not (ark/classifier? classifier))
    (throw (Exception. (str classifier " is not a classifier keyword"))))
  (clj-uuid/v5 clj-uuid/+null+ (name classifier)))

(defn build
  [m]
  (-> m
      (assoc :uuid/journal-entry-uuid journal-entry-uuid)
      (assoc :uuid/random-uuid random-uuid)
      (assoc :uuid/index-uuid index-uuid)))
