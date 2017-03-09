(ns console.server
  (:require [tiples.users :as users]
            [tiples.server :as tiples]
            [simpleArk.arkDb.ark-db :as ark-db]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.miMap :as miMap]
            [simpleArk.uuid.uuid :as uuid]))

(defn initializer
  []
  (uuid/register users/ark-db)
  (miMap/register users/ark-db)
  (arkRecord/register users/ark-db)
  (rolonRecord/register users/ark-db)
  )

(defn notify-colsole
  [je-uuid]
  (users/broadcast! :console/update (ark-db/get-ark-record users/ark-db)))

(defmethod tiples/event-msg-handler :console/process-transaction
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [client-id (:client-id ev-msg)
        tran-keyword (:tran-keyword ?data)
        tran-data (:tran-data ?data)
        session (@users/session-record-by-client-id client-id)
        user-uuid (if session
                    (:user-uuid session)
                    nil)]
    (when (users/get-client-capability-uuid :console client-id)
      (try
        (println :transaction tran-keyword tran-data)
        (let [je-uuid (ark-db/process-transaction! users/ark-db user-uuid :console tran-keyword tran-data)]
          ;(update-ark-record! (ark-db/get-ark-record users/ark-db))
          (tiples/chsk-send! client-id [:console/transaction-response (str je-uuid)]))
        (catch Exception e
          (tiples/chsk-send! client-id [:console/error (.getMessage e)]))))))

(defmethod users/get-common :console [capability-kw]
  (ark-db/get-ark-record users/ark-db))
