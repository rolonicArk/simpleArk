(ns profile.server
  (:require
    [tiples.server :as tiples]
    [tiples.users :as users]
    [simpleArk.builder :as builder]))

(defmethod tiples/event-msg-handler :profile/update
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        user-uuid (users/get-client-user-uuid client-id)
        local {}
        user-profile-uuid (users/get-user-capability-uuid :profile user-uuid)
        actions (builder/build-replace-map [] ?data [:content/data] user-profile-uuid)]
    (builder/transaction! users/ark-db user-uuid local actions)))
