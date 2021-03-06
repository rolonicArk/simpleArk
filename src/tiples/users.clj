(ns tiples.users
  (:require [tiples.server :as tiples]
            [simpleArk.arkDb.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.reader :as reader]
            [simpleArk.log.logt :as logt]
            [simpleArk.pub.pub0 :as pub0]
            [simpleArk.sub.sub0 :as sub0]
            [simpleArk.tlog.tlog1 :as tlog1]
            [simpleArk.arkDb.ark-dba1 :as ark-dba1]
            [simpleArk.arkValue.ark-value0 :as ark-value0]
            [simpleArk.uuid.uuidi :as uuidi]
            [simpleArk.closer :as closer]
            [simpleArk.mapish :as mapish]))

(def ark-db ((comp
               (ark-db/builder)
               (sub0/builder)
               (pub0/builder)
               (tlog1/builder)
               (ark-dba1/builder)
               (ark-value0/builder)
               (uuidi/builder)
               (closer/builder)
               (logt/builder)
               (reader/builder))
              {}))

(def capabilities (atom []))

(defn add-capability!
  [capability]
  (if (= -1 (.indexOf @capabilities capability))
    (swap! capabilities conj capability)))

(defrecord SessionRecord [client-id user-name user-capabilities user-uuid])

(def session-record-by-client-id (atom {}))
(def session-record-by-user-name (atom {}))
(def session-record-by-user-uuid (atom {}))

(defn get-client-user-uuid
  [client-id]
  (let [session-record (@session-record-by-client-id client-id)]
    (if session-record
      (:user-uuid session-record)
      nil)))

(defmulti get-common identity)

(defn get-capability-index-uuid
  [ark-record]
  (arkRecord/get-index-uuid ark-record "capability-name"))

(defn get-capability-uuid
  [ark-record capability-kw]
  (let [capabiity-name (name capability-kw)
        capability-index-uuid (get-capability-index-uuid ark-record)]
    (first (arkRecord/index-lookup ark-record capability-index-uuid capabiity-name))))

(defn get-user-capability-uuid
  ([capability-kw user-uuid]
   (get-user-capability-uuid (ark-db/get-ark-record ark-db)
                             capability-kw
                             user-uuid))
  ([ark-record capability-kw user-uuid]
   (arkRecord/get-link-value ark-record user-uuid :inv-rel/user capability-kw)))

(defn get-user-capability-data
  [ark-record user-capability-uuid]
  (reduce
    (fn [m e]
      (assoc m (second (key e)) (val e)))
    {}
    (mapish/mi-sub
      (arkRecord/get-property-values ark-record user-capability-uuid)
      [:content/data])))

(defn get-user-data
  ([user-uuid]
   (get-user-data (ark-db/get-ark-record ark-db) user-uuid))
  ([ark-record user-uuid]
   (let [inv-user-properties
         (mapish/mi-sub
           (arkRecord/get-property-values ark-record user-uuid)
           [:inv-rel/user])]
     (reduce
       (fn [m e]
         (let [user-capability-uuid (nth (key e) 2)
               capability-data (get-user-capability-data ark-record user-capability-uuid)
               capability-kw (nth (key e) 1)]
           (assoc m capability-kw capability-data)))
       {}
       inv-user-properties))))

(defn get-client-capability-uuid
  [capability-kw client-id]
  (get-user-capability-uuid (ark-db/get-ark-record ark-db)
                            capability-kw
                            (get-client-user-uuid client-id)))

(defn broadcast! [msg-id data]
  (let [uids (keys @session-record-by-client-id)
        msg [msg-id data]]
    (doseq [uid uids]
      (tiples/chsk-send! uid msg))))

(defn close-session
  [session-record]
  (swap! session-record-by-client-id dissoc (:client-id session-record))
  (swap! session-record-by-user-name dissoc (:user-name session-record))
  (swap! session-record-by-user-uuid dissoc (:user-uuid session-record))
  (broadcast! :users/logged-in-notice [(:user-name session-record) nil])
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

(defn select-common
  [user-capabilities]
  (reduce
    (fn [m c]
      (assoc m c (get-common c)))
    {}
    user-capabilities))

(defn add-session
  [client-id user-name user-uuid]
  (let [session-record (@session-record-by-client-id client-id)]
    (if session-record
      (logout session-record)))
  (let [session (@session-record-by-user-name user-name)]
    (if session
      (logout session)))
  (let [user-data (get-user-data user-uuid)
        user-capabilities (keys user-data)
        select-capabilities (reduce
                              (fn [r a]
                                (if (user-data a)
                                  (conj r a)
                                  r))
                              []
                              @capabilities)
        session (->SessionRecord client-id user-name user-capabilities user-uuid)
        select-common-data (select-common user-capabilities)]
    (swap! session-record-by-client-id assoc client-id session)
    (swap! session-record-by-user-name assoc user-name session)
    (swap! session-record-by-user-uuid assoc (:user-uuid session) session)
    (tiples/chsk-send! client-id
                       [:users/logged-in
                        [select-capabilities select-common-data user-data]
                        ]
                       )
    (broadcast! :users/logged-in-notice [user-name user-capabilities])
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
