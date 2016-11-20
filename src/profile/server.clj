(ns profile.server
  (:require
    [tiples.server :as tiples]
    [tiples.users :as users]))

(defmethod tiples/event-msg-handler :profile/update
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)]
    (users/swap-client-data! :profile
                             client-id
                             (fn [old-cd]
                               ?data))))
