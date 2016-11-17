(ns console.server
  (:require [tiples.users :as users]))

(users/add-capability :console)

(defn update-ark-record!
  [ark-record]
  (swap! users/common-data
         (fn [common]
           (assoc common :console ark-record))))
