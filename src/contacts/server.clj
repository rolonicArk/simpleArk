(ns contacts.server
  (:require [tiples.users :as users]
            [tiples.server :as tiples]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.mapish :as mapish]))

(defn add-contact!
  [contact]
  (swap! users/common-data
         (fn [common]
           (let [contacts-set (get common :contacts #{})
                 contacts-set (conj contacts-set contact)]
             (assoc common :contacts contacts-set)))))

(defmethod tiples/event-msg-handler :contacts/delete
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        contact (:contact ?data)]
    (when (users/get-client-capability-uuid :contacts client-id)
      (when (users/swap-common-data! :contacts
                                   (fn [contacts]
                                     (disj contacts contact))
                                   #{})
        (users/broadcast! :contacts/deleted contact)))))

(defmethod tiples/event-msg-handler :contacts/add
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        contact (:contact ?data)]
    (when (users/get-client-capability-uuid :contacts client-id)
      (when (users/swap-common-data! :contacts
                                   (fn [contacts] (conj contacts contact))
                                   #{})
        (users/broadcast! :contacts/added contact)))))

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
