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

(def transaction-je-uuid-string (j/cell nil))
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

(defmethod tiples/chsk-recv :console/transaction-response
  [id je-uuid-string]
  (reset! transaction-je-uuid-string je-uuid-string))

(defmethod tiples/chsk-recv :console/error
  [id msg]
  (reset! transaction-error true)
  (reset! transaction-error-msg msg))

(defn je-count [ark-record]
  (if ark-record
    (str "journal entry rolons count: " (count (arkRecord/get-journal-entries ark-record)) "\n\n")
    ""))

(defn indexes-count [ark-record]
  (if ark-record
    (str "index rolons count: " (count (arkRecord/get-indexes ark-record)) "\n\n")
    ""))

(defn application-rolons-count [ark-record]
  (if ark-record
    (str "application rolons count: " (count (arkRecord/get-application-rolons ark-record)) "\n\n")
    ""))

(defmethod login/add-header-element :console [_]
  (h/div
    (h/h2 "Ark Console")
    (h/p "?")
    ))

(defn fred []
  (tiples/chsk-send! [:console/process-transaction {:tran-keyword :hello-world! :tran-data "Fred"}]))

(defn make-bob []
  (tiples/chsk-send!
    [:console/process-transaction
     {:tran-keyword
      :ark/update-rolon-transaction!
      :tran-data
      (prn-str
        [nil
         {[:index/headline] "make bob"}
         {[:content/age]             8
          [:index/name]              "Bob"
          [:index/headline] "First application Rolon"
          [:content/brothers "John"] true
          [:content/brothers "Jeff"] true}])
      }]))

(j/defc= consoleheader-element nil)

(defn td-style [width]
  (str "width:" (/ width 2) "px"))

(defn tx-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (- windowInnerHeight header-height 50) "px;vertical-align:bottom")))

(j/defc output [])

(defn default-style
  []
  "")

(defn bold-style
  []
  "font-weight: bold")

(defn italic-style
  []
  "font-style: italic")

(defn clickable-style
  []
  "color:blue;cursor:pointer")

(defn no-click [arg])

(defn log-click [arg]
  (.log js/console arg))

(defn add-output!
  ([txt] (add-output! txt default-style))
  ([txt style] (add-output! txt style no-click nil))
  ([txt style on-click arg]
   (swap! output (fn [old]
                   (let [v [(str "disp" (count old)) txt (style) on-click arg]
                         nw (conj old v)]
                     (h/with-timeout
                       0
                       (let [e (.getElementById js/document (str "disp" (- (count nw) 1)))]
                         (if (some? e)
                           (.scrollIntoView e true))))
                     nw)))))

(defn my-ark-record-updated [_ _ _ n]
  (add-output! "\n***ark updated***\n" italic-style)
  )

(add-watch my-ark-record :my-ark-record my-ark-record-updated)

(defn list-headlines [ark-record]
  (add-output! "headlines:\n" bold-style)
  (let [index-uuid (arkRecord/get-index-uuid ark-record "headline")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (doall (map #(add-output! (str (first %) "\n") clickable-style log-click "headline") content-index)))
  (add-output! "\n"))

(defn list-transaction-names [ark-record]
  (add-output! "transaction names:\n" bold-style)
  (let [index-uuid (arkRecord/get-index-uuid ark-record "transaction-name")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    ;(mapish/debug [:content content-index])
    (doall (map #(add-output! (str (first %) "\n") clickable-style log-click "transaction") content-index)))
  (add-output! "\n"))

(defn list-index-names [ark-record]
  (add-output! "index names:\n" bold-style)
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        arkRecord/index-name-uuid)]
    (doall (map #(add-output! (str (first %) "\n") clickable-style log-click "index") content-index)))
  (add-output! "\n"))

(defn list-application-names [ark-record]
  (add-output! "application names:\n" bold-style)
  (let [index-uuid (arkRecord/get-index-uuid ark-record "name")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (doall (map #(add-output! (str (first %) "\n") clickable-style log-click "index") content-index)))
  (add-output! "\n"))

(def do-console
  (h/div
    (h/table :style "width:100%"
             (h/tr
               (h/td :style (j/cell= (td-style login/windowInnerWidth))
                     (h/div :style (j/cell= (tx-style login/windowInnerHeight login/header-height))

                            (h/button
                              :click (fn []
                                       (reset! output []))
                              "clear display")

                            (h/table
                              (h/tr
                                (h/th "Rolon Counts:")
                                (h/td
                                  (h/button
                                    :click (fn []
                                             (add-output! (application-rolons-count @my-ark-record)))
                                    "applications"))
                                (h/td
                                  (h/button
                                    :click (fn []
                                             (add-output! (indexes-count @my-ark-record)))
                                    "indexes"))
                                (h/td
                                  (h/button
                                    :click (fn []
                                             (add-output! (je-count @my-ark-record)))
                                    "journal entries")
                                  )))

                            (h/output (h/strong "Index content: "))

                            (h/button
                              :click (fn []
                                       (list-index-names @my-ark-record))
                              "indexes")

                            (h/button
                              :click (fn []
                                       (list-headlines @my-ark-record))
                              "headlines")

                            (h/button
                              :click (fn []
                                       (list-transaction-names @my-ark-record))
                              "transaction names")

                            (h/button
                              :click (fn []
                                       (list-application-names @my-ark-record))
                              "application names")

                            (h/div
                              :style "color:red"
                              (h/p (h/text (if transaction-error
                                             (str "Error: " transaction-error-msg)
                                             "")))
                              )

                            (h/output (h/strong "Transactions: "))

                            (h/button
                              :click #(fred)
                              :href ""
                              "Hello Fred")

                            (h/button
                              :click #(make-bob)
                              :href ""
                              "Make Bob")

                            (h/button
                              :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :invalid :tran-data ""}])
                              "Invalid!")

                            (h/button
                              :click #(tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}])
                              "Trouble!")

                            (h/p
                              (h/text
                                (if (some? transaction-je-uuid-string)
                                  (str "Last requested transaction Journal Entry UUID: " transaction-je-uuid-string)
                                  "")))
                            ))

               (h/td :style (j/cell= (td-style login/windowInnerWidth))
                     (h/div :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
                            (h/div :style "white-space:pre-wrap"
                                   (h/for-tpl [[txt-id txt style on-click arg] output]
                                              (h/output :id txt-id
                                                        :style style
                                                        :click (fn [] (@on-click @arg))
                                                        txt))))
                     )))))

(defmethod login/add-body-element :console [_]
  (do-console))
