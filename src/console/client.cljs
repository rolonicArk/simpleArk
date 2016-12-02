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
    [tiples.client :as tiples]
    [simpleArk.mapish :as mapish]))

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
    (str "journal entry rolons count: " (count (arkRecord/get-journal-entries ark-record)))
    ""))

(defn indexes-count [ark-record]
  (if ark-record
    (str "index rolons count: " (count (arkRecord/get-indexes ark-record)))
    ""))

(defn application-rolons-count [ark-record]
  (if ark-record
    (str "application rolons count: " (count (arkRecord/get-application-rolons ark-record)))
    ""))

(defmethod login/add-header-element :console [_]
  (h/div
    (h/h2 "Ark Console")
    (h/p "?")
    ))

(defn fred []
  (tiples/chsk-send! [:console/process-transaction {:tran-keyword :hello-world! :tran-data "Fred"}]))

(j/defc= consoleheader-element nil)

(defn td-style [width]
  (str "width:" (/ width 2) "px"))

(defn tx-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (- windowInnerHeight header-height 50) "px;vertical-align:bottom")))

(j/defc output [])

(defn add-output! [line]
  ;(mapish/debug [:line line])
  (swap! output (fn [old]
                  (conj old [(str "disp" (count old)) (str line)])))
  (h/with-timeout 0
                  (let [e (.getElementById js/document (str "disp" (- (count @output) 1)))]
                    (if (some? e)
                      (.scrollIntoView e true)))))

(defn list-headlines [ark-record]
  (add-output! "headlines:")
  (let [index-uuid (arkRecord/get-index-uuid ark-record "headline")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (doall (map #(add-output! (first %)) content-index)))
  (add-output! " "))

  (defn list-transaction-names [ark-record]
    (add-output! "transaction names:")
    (let [index-uuid (arkRecord/get-index-uuid ark-record "transaction-name")
          content-index (arkRecord/get-content-index
                          ark-record
                          index-uuid)]
      ;(mapish/debug [:content content-index])
      (doall (map #(add-output! (first %)) content-index)))
    (add-output! " "))

(defn list-index-names [ark-record]
  (add-output! "index names:")
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        arkRecord/index-name-uuid)]
    (doall (map #(add-output! (first %)) content-index)))
  (add-output! " "))

(def do-console
  (h/div
         (h/table :style "width:100%"
           (h/tr
             (h/td :style (j/cell= (td-style  login/windowInnerWidth))
               (h/div :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
                      (h/table
                        (h/tr
                          (h/th "Rolon Counts:")
                          (h/td
                            (h/button
                              :click #(add-output! (application-rolons-count @my-ark-record))
                              "applications"))
                          (h/td
                            (h/button
                              :click #(add-output! (indexes-count @my-ark-record))
                              "indexes"))
                          (h/td
                            (h/button
                              :click #(add-output! (je-count @my-ark-record))
                              "journal entries")
                            )))

                      (h/button
                        :click (fn []
                                 (list-index-names @my-ark-record))
                        "list indexes")

                      (h/button
                        :click (fn []
                                 (list-headlines @my-ark-record))
                        "list headlines")

                      (h/button
                        :click (fn []
                                 (list-transaction-names @my-ark-record))
                        "list transaction names")

                      (h/div
                        :style "color:red"
                        (h/p (h/text (if transaction-error
                                       (str "Error: " transaction-error-msg)
                                       "")))
                        )

                      (h/p (h/button
                             :click #(fred)
                             :href ""
                             "Hello Fred"))

                      (h/p "games "
                           (h/button
                             :toggle false
                             :click nil
                             nil))

                      (h/p
                        (h/button
                          :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :invalid :tran-data ""}])
                          "Invalid!"))

                      (h/p
                        (h/button
                          :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}])
                          "Trouble!"))))

             (h/td :style (j/cell= (td-style  login/windowInnerWidth))
                   (h/div :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
                          (h/div :style "white-space:pre-wrap;font-family:\"Lucida Console\", monospace"
                                 (h/for-tpl [[line-id txt] output]
                                            (h/div
                                              :id line-id
                                              txt))))
                    )))))

(defmethod login/add-body-element :console [_]
  (do-console))
