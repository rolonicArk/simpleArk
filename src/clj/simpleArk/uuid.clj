(ns simpleArk.uuid
  (:require [clj-uuid :as uuid]))

(defn journal-entry-uuid
  [_]
  (uuid/v1))

(defn random-uuid
  [_]
  (uuid/v4))

(defn index-uuid
  [_ classifier]
  (if (not (classifier? classifier))
    (throw (Exception. (str classifier " is not a classifier keyword"))))
  (uuid/v5 uuid/+null+ (name classifier)))

(defn journal-entry-uuid?
  [_ uuid]
  (and (uuid? uuid)
       (= (uuid/get-version uuid) 1)))

(defn random-uuid?
  [_ uuid]
  (and (uuid? uuid)
       (= (uuid/get-version uuid) 4)))

(defn index-uuid?
  [_ uuid]
  (and (uuid? uuid)
       (= (uuid/get-version uuid) 5)))

;;a well known uuid
(def index-name-uuid (index-uuid _ :classifier/index.name))
