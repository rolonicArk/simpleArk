(ns console.client
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [cljs.reader :as reader]
    [simpleArk.uuid :as suuid]
    [simpleArk.miView :as miView]
    [simpleArk.miMap :as miMap]
    [simpleArk.rolonRecord :as rolonRecord]
    [simpleArk.arkRecord :as arkRecord]
    [tiples.login :as login]
    [tiples.client :as tiples]))

(def transaction-error (j/cell false))
(def transaction-error-msg (j/cell ""))

(j/defc= my-ark-record
       (if login/common-data
         (:console login/common-data)
         nil)
       (partial swap! login/common-data assoc :console))

(defmethod tiples/chsk-recv :console/update
  [id ark-record]
  (reset! transaction-error false)
  (reset! my-ark-record ark-record))

(defmethod tiples/chsk-recv :console/error
  [id msg]
  (reset! transaction-error true)
  (reset! transaction-error-msg msg))

(defn je-count [ark-record]
  (if ark-record
    (str "journal entry count: " (count (arkRecord/get-journal-entries ark-record)))
    ""))

(defn indexes-count [ark-record]
  (if ark-record
    (str "indexes count: " (count (arkRecord/get-indexes ark-record)))
    ""))

(defn application-rolons-count [ark-record]
  (if ark-record
    (str "application rolons count: " (count (arkRecord/get-application-rolons ark-record)))
    ""))

(def do-console
  (h/div
    (h/div :id "header"
         :style "background-color:#f0f0ff"
         (login/tabs-div)
         (h/h2 "Ark Console"))
    (h/div
      (h/p (h/text (je-count my-ark-record)))
      (h/p (h/text (indexes-count my-ark-record)))
      (h/p (h/text (application-rolons-count my-ark-record)))

      (h/div
        :slide-toggle transaction-error
        :css {:display "none"}
        :style "color:red"
        (h/p (h/text (str "Error: " transaction-error-msg)))
        )

      (h/button
        :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :hello-world! :tran-data "Fred"}])
        "Hello Fred")

      (h/button
        :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :invalid :tran-data ""}])
        "Invalid!")

      (h/button
        :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}])
        "Trouble!")
      )))

(defmethod login/add-element :console [_]
  (do-console))
