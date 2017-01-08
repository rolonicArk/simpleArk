(ns console.server
  (:require [tiples.users :as users]
            [tiples.server :as tiples]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.ark-db0 :as ark-db0]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.closer :as closer]
            [simpleArk.logt :as logt]
            [simpleArk.reader :as reader]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.miMap :as miMap]
            [simpleArk.uuid :as uuid]))

(defn update-ark-record!
  [ark-record]
  (swap! users/common-data
         (fn [common]
           (assoc common :console ark-record))))

(defn initializer
  []
  (uuid/register users/ark-db)
  (miMap/register users/ark-db)
  (arkRecord/register users/ark-db)
  (rolonRecord/register users/ark-db)
  )

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
        (let [je-uuid (ark-db/process-transaction! users/ark-db user-uuid tran-keyword tran-data)]
          (update-ark-record! (ark-db/get-ark-record users/ark-db))
          (users/broadcast! :console/update (ark-db/get-ark-record users/ark-db))
          (tiples/chsk-send! client-id [:console/transaction-response (str je-uuid)]))
        (catch Exception e
          (tiples/chsk-send! client-id [:console/error (.getMessage e)]))))))
