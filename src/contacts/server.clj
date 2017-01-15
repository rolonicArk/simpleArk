(ns contacts.server
  (:require [tiples.users :as users]
            [tiples.server :as tiples]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.mapish :as mapish]
            [simpleArk.builder :as builder]
            [welcome.demo-builds :as demo-builds]))

(defmethod tiples/event-msg-handler :contacts/delete
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        contact (:contact ?data)]
    (when (users/get-client-capability-uuid :contacts client-id)
      (let [user-uuid (users/get-client-user-uuid client-id)
            contacts-capability (users/get-capability-uuid (ark-db/get-ark-record users/ark-db) :contacts)]
        (builder/transaction!
          users/ark-db
          user-uuid
          {:local/contacts-capability contacts-capability}
          (-> []
              (builder/build-je-property [:index/headline] "Delete contact")
              (demo-builds/update-contact contact nil))))
        (users/broadcast! :contacts/deleted contact))))

(defmethod tiples/event-msg-handler :contacts/add
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        contact (:contact ?data)]
    (when (users/get-client-capability-uuid :contacts client-id)
      (let [user-uuid (users/get-client-user-uuid client-id)
            contacts-capability (users/get-capability-uuid (ark-db/get-ark-record users/ark-db) :contacts)]
        (builder/transaction!
          users/ark-db
          user-uuid
          {:local/contacts-capability contacts-capability}
          (-> []
              (builder/build-je-property [:index/headline] "Delete contact")
              (demo-builds/update-contact contact true))))
      (users/broadcast! :contacts/added contact))))

(defmethod users/get-common :contacts [capability-kw]
  (let [ark-record (ark-db/get-ark-record users/ark-db)
        capability-uuid (users/get-capability-uuid ark-record :contacts)
        properties (arkRecord/get-property-values ark-record capability-uuid)
        properties (mapish/mi-sub properties [:content/contact])]
    (reduce
      (fn [s e]
        (let [path (key e)
              m (into {} (next path))]
          (conj s m)))
      #{}
      properties)))
