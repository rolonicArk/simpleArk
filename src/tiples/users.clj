(ns tiples.users
  (:require [tiples.server :as tiples]
            [com.rpl.specter :as s]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.reader :as reader]
            [simpleArk.logt :as logt]
            [simpleArk.ark-db0 :as ark-db0]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.closer :as closer]))

(def ark-db ((comp
               (ark-db/builder)
               (ark-db0/builder)
               (ark-value0/builder)
               (uuidi/builder)
               (closer/builder)
               (logt/builder)
               (reader/builder))
              {}))

(def capabilities (atom []))

(def common-data (atom {}))

(defn add-capability!
  [capability]
  (if (= -1 (.indexOf @capabilities capability))
    (swap! capabilities conj capability)))

(defrecord UserRecord [name user-data])

(def user-records (atom (sorted-map)))

(defn add-user!
  [name user-data]
  (swap! user-records assoc name (->UserRecord name user-data)))

(defn get-user-record
  [name]
  (@user-records name))

(defrecord SessionRecord [client-id name user-capabilities user-uuid])

(def session-record-by-client-id (atom {}))
(def session-record-by-name (atom {}))

(defn get-client-user-record
  [client-id]
  (let [session (@session-record-by-client-id client-id)]
    (if session
      (get-user-record (:name session))
      nil)))

(defn get-client-user-uuid
  [client-id]
  (let [session (@session-record-by-client-id client-id)]
    (if session
      (:user-uuid session)
      nil)))

(defn get-common-data
  [capability]
  (capability @common-data))

(defn swap-common-data!
  [capability f default]
  (swap! common-data
         (fn [cd]
           (let [capability-data (get cd capability default)
                 capability-data (f capability-data)]
             (assoc cd capability capability-data)))))

(defn get-client-capability-data
  [capability client-id]
  (let [client (get-client-user-record client-id)]
    (if client
      (let [user-data (:user-data client)]
        (get user-data capability))
      nil)))

(defn valid-user-data?
  [capability name]
  (let [user (@user-records name)
        user-data (if user
                    (get user :user-data)
                    nil)]
    (if user-data
      (if (get user-data capability)
        true
        false)
      false)))

(defn swap-user-data!
  [capability name f]
  (if (valid-user-data? capability name)
    (do
      (swap! user-records (fn [us] (s/transform [(s/keypath name) :user-data capability] f us)))
      true)
    false))

(defn swap-client-data!
  [capability client-id f]
    (let [session (@session-record-by-client-id client-id)]
      (if session
        (swap-user-data! capability (:name session) f)
        false)))

(defn broadcast! [msg-id data]
  (let [uids (keys @session-record-by-client-id)
        msg [msg-id data]]
    (doseq [uid uids]
      (tiples/chsk-send! uid msg))))

(defn close-session
  [session]
  (swap! session-record-by-client-id dissoc (:client-id session))
  (swap! session-record-by-name dissoc (:name session))
  (broadcast! :users/logged-in-notice [(:name session) nil])
  )

(defn logout
  [session]
  (tiples/chsk-send! (:client-id session) [:users/logged-in nil])
  (close-session session))

(defmethod tiples/event-msg-handler :users/logout
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        session (@session-record-by-client-id client-id)]
    (if session
      (logout session)))
  )

(defmethod tiples/event-msg-handler :chsk/uidport-close
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        session (@session-record-by-client-id client-id)]
    (if session
      (close-session session)))
  )

(defn add-session
  [client-id name user-uuid]
  (let [session (@session-record-by-client-id client-id)]
    (if session
      (logout session)))
  (let [session (@session-record-by-name name)]
    (if session
      (logout session)))
  (let [user (get-user-record name)
        user-data (:user-data user)
        user-capabilities (keys user-data)
        select-capabilities (reduce
                              (fn [r a]
                                (if (user-data a)
                                  (conj r a)
                                  r))
                              []
                              @capabilities)
        session (->SessionRecord client-id name user-capabilities user-uuid)
        select-common-data (select-keys @common-data user-capabilities)]
    (swap! session-record-by-client-id assoc client-id session)
    (swap! session-record-by-name assoc name session)
    (tiples/chsk-send! client-id
                       [:users/logged-in
                        [select-capabilities select-common-data user-data]
                        ]
                       )
    (broadcast! :users/logged-in-notice [name user-capabilities])
    ))

(defmethod tiples/event-msg-handler :users/login
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        name (:name ?data)
        ark-record (ark-db/get-ark-record ark-db)
        index-uuid (arkRecord/get-index-uuid ark-record "user-name")
        user-uuid (first (arkRecord/index-lookup ark-record index-uuid name))
        real-password (if user-uuid
            (arkRecord/get-property-value ark-record user-uuid [:content/password])
            nil)
        password (:password ?data)]
    (if (and user-uuid (= password real-password))
      (add-session client-id name user-uuid)
      (do
        (tiples/chsk-send! client-id [:users/login-error nil])
        ))))
