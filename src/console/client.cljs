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
    [simpleArk.mapish :as mapish]
    [cljs-time.core :as time]
    [cljs-time.coerce :as cotime]
    [cljs-time.format :as ftime]))

(def format-time (ftime/formatters :mysql))

(def transaction-je-uuid-string (j/cell nil))
(def transaction-error (j/cell false))
(def transaction-error-msg (j/cell ""))

(def selected-time (j/cell ""))
(def old-ark-record (j/cell nil))

(j/defc= my-ark-record
         (if login/common-data
           (let [n (:console login/common-data)]
             (if (= "" selected-time)
               n
               (arkRecord/select-time n (suuid/create-uuid selected-time))))
           nil)
         (partial swap! login/common-data assoc :console))

(j/defc= latest-journal-entry-uuid
         (arkRecord/get-latest-journal-entry-uuid my-ark-record))

(j/defc output [])

(j/defc= consoleheader-element nil)

(def selected-index (j/cell ""))

(def selected-rolon (j/cell ""))
(def alternate-rolon (j/cell ""))

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

(defmethod login/add-header-element :console [_]
  (h/div
    (h/h2 "Ark Console")

    (h/button
      :click (fn []
               (reset! output []))
      "clear display")))

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
          [:index/headline]          "First application Rolon"
          [:content/brothers "John"] true
          [:content/brothers "Jeff"] true}])
      }]))

(defn td-style [width]
  (str "width:" (/ width 2) "px"))

(defn tx-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (- windowInnerHeight header-height 50) "px;vertical-align:bottom")))

(defn default-style
  []
  "")

(defn command-style
  []
  "font-weight:bold; display:block; background-color:MistyRose")

(defn command-prefix-style
  []
  "font-weight:bold; background-color:MistyRose")

(defn block-style
  []
  "display:block")

(defn event-style
  []
  "font-style:italic; display:block; background-color:yellow")

(defn selection-style
  []
  "font-style:italic; background-color:lightGrey")

(defn clickable-je-style
  []
  "color:orange;cursor:pointer")

(defn clickable-index-style
  []
  "color:blue;cursor:pointer")

(defn clickable-application-style
  []
  "color:green;cursor:pointer")

(defn clickable-styles
  [uuid]
  (cond
    (suuid/journal-entry-uuid? uuid)
    clickable-je-style
    (suuid/index-uuid? uuid)
    clickable-index-style
    (suuid/random-uuid? uuid)
    clickable-application-style
    ))

(defn no-click [arg])

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

(defn pretty-uuid
  [ark-record uuid]
  (cond
    (suuid/journal-entry-uuid? uuid)
    (do
      (let [tm (suuid/get-time uuid)
            dt (cotime/to-date-time tm)
            ldt (time/to-default-time-zone dt)]
        (ftime/unparse format-time ldt)))
    :else
    (str uuid)))

(defn rolon-click [ark-record arg]
  (let [uuid (suuid/create-uuid arg)]
    (reset! selected-rolon arg)
    (add-output! "\nselected rolon:" selection-style)
    (add-output! " ")
    (add-output! (pretty-uuid ark-record uuid) (clickable-styles uuid))))

(defn alternate-click [ark-record arg]
  (let [uuid (suuid/create-uuid arg)]
    (reset! alternate-rolon arg)
    (add-output! "\nalternate rolon:" selection-style)
    (add-output! " ")
    (add-output! (pretty-uuid ark-record uuid) (clickable-styles uuid))))

(defn uuid-click [ark-record arg]
  (let [uuid (suuid/create-uuid arg)]
  (cond
    (suuid/index-uuid? uuid)
    (do
      (reset! selected-index arg)
      (add-output! "\nselected index:" selection-style)
      (add-output! " ")
      (add-output! (pretty-uuid ark-record uuid) (clickable-styles uuid)))
    (suuid/journal-entry-uuid? uuid)
    (do
      (reset! old-ark-record (:console @login/common-data))
      (reset! selected-time arg)
      (add-output! "\nselected time:" selection-style)
      (add-output! " ")
      (add-output! (pretty-uuid ark-record uuid) (clickable-styles uuid)))
    (suuid/random-uuid? uuid)
    (rolon-click ark-record arg))))

(defn my-ark-record-updated [_ _ _ n]
  (if (and (= "" @selected-time)
           (not= n @old-ark-record))
    (add-output! "***ark updated***" event-style)))

(add-watch my-ark-record :my-ark-record my-ark-record-updated)

(defn je-count [ark-record]
  (add-output! "> journal entry rolons count: " command-prefix-style)
  (add-output! (str (count (arkRecord/get-journal-entries ark-record)) "\n")))

(defn indexes-count [ark-record]
  (add-output! "> index rolons count: " command-prefix-style)
  (add-output! (str (count (arkRecord/get-indexes ark-record)) "\n")))

(defn application-rolons-count [ark-record]
  (add-output! "> application rolons count: " command-prefix-style)
  (add-output! (str (count (arkRecord/get-application-rolons ark-record)) "\n")))

(defn display-index
  [content-index]
  ;(.log js/console (pr-str :display-index content-index))
  (doall (map (fn [kv]
                (let [k (str (key kv) " ")
                      v (val kv)]
                  (add-output! k)
                  (add-output! (str (pretty-uuid my-ark-record v) "\n")
                               (clickable-styles v) uuid-click (str v))))
              content-index)))

(defn list-index-content [ark-record index-uuid]
  (add-output! "> list index content:" command-style)
  (add-output! "index: ")
  (add-output! (str (pretty-uuid my-ark-record index-uuid) "\n") (clickable-styles index-uuid) uuid-click (str index-uuid))
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-index content-index)))

(defn list-headlines [ark-record]
  (add-output! "> headlines:" command-style)
  (let [index-uuid (arkRecord/get-index-uuid ark-record "headline")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-index content-index)))

(defn list-transaction-names [ark-record]
  (add-output! "> transaction names:" command-style)
  (let [index-uuid (arkRecord/get-index-uuid ark-record "transaction-name")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    ;(mapish/debug [:content content-index])
    (display-index content-index)))

(defn list-index-names [ark-record]
  (add-output! "> list indexes:" command-style)
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        arkRecord/index-name-uuid)]
    (display-index content-index)))

(defn list-application-names [ark-record]
  (add-output! "> application names:" command-style)
  (let [index-uuid (arkRecord/get-index-uuid ark-record "name")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-index content-index)))

(def do-console
  (h/div
    (h/table :style "width:100%"
             (h/tr
               (h/td :style (j/cell= (td-style login/windowInnerWidth))
                     (h/div :style (j/cell= (tx-style login/windowInnerHeight login/header-height))

                            (h/div
                              (h/span
                                (h/strong
                                  "Selected time: "))
                              (h/span
                                :style (j/cell= (if (= "" selected-time)
                                                  ""
                                                  "color:green;cursor:pointer"
                                                  ))
                                :click #(rolon-click @my-ark-record @selected-time)
                                (h/text
                                  (if (= "" selected-time)
                                    "now"
                                    (pretty-uuid my-ark-record (suuid/create-uuid selected-time))))))

                            (h/div
                              :css {:display "none"}
                              :toggle (j/cell= (not= "" selected-time))

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (add-output! "> clear time selection" command-style)
                                         (reset! selected-time ""))
                                "clear time selection")
                              )

                            (h/hr)

                            (h/div
                              :css {:display "none"}
                              :toggle (j/cell= (some? transaction-je-uuid-string))
                              (h/span
                                (h/strong "My last Journal Entry: "))
                              (h/span
                                :style "color:orange;cursor:pointer"
                                :click #(uuid-click @my-ark-record @transaction-je-uuid-string)
                                (h/text (pretty-uuid my-ark-record (suuid/create-uuid transaction-je-uuid-string)))))

                            (h/div
                              :css {:display "none"}
                              :toggle (j/cell= (some? latest-journal-entry-uuid))
                              (h/span
                                (h/strong "Latest Journal Entry: "))
                              (h/span
                                :style "color:orange;cursor:pointer"
                                :click #(uuid-click @my-ark-record (str @latest-journal-entry-uuid))
                                (h/text
                                  (pretty-uuid my-ark-record latest-journal-entry-uuid)))
                              )

                            (h/hr)

                            (h/div
                              (h/span
                                (h/strong "Selected Index: "))
                              (h/span
                                :style (j/cell= (if (= "" selected-index)
                                                  ""
                                                  "color:green;cursor:pointer"
                                                  ))
                                :click #(rolon-click @my-ark-record @selected-index)
                                (h/text
                                  (if (= "" selected-index)
                                    "none"
                                    (pretty-uuid my-ark-record (suuid/create-uuid selected-index))))))

                            (h/div

                              (h/button
                                :css {:display "none" :background-color "MistyRose"}
                                :toggle (j/cell= (not= "" selected-index))
                                :click (fn []
                                         (add-output! "> clear index selection" command-style)
                                         (reset! selected-index ""))
                                "clear index selection")

                              (h/button
                                :css {:display "none" :background-color "MistyRose"}
                                :toggle (j/cell= (not= "" selected-index))
                                :click (fn []
                                         (list-index-content @my-ark-record
                                                             (suuid/create-uuid @selected-index)))
                                "list index content")

                            (h/button
                              :style "background-color:MistyRose"
                              :click (fn []
                                       (list-index-names @my-ark-record))
                              "list indexes"))

                            (h/hr)

                            (h/div
                              (h/span
                                (h/strong "Selected Rolon: "))
                              (h/span
                                :style (j/cell= (if (= "" selected-rolon)
                                                  ""
                                                  "color:green;cursor:pointer"
                                                  ))
                                :click #(alternate-click @my-ark-record @selected-rolon)
                                (h/text
                                  (if (= "" selected-rolon)
                                    "none"
                                    (pretty-uuid my-ark-record (suuid/create-uuid selected-rolon))))))

                            (h/div
                              :css {:display "none"}
                              :toggle (j/cell= (not= "" selected-rolon))

                              (h/button
                                :css {:display "none" :background-color "MistyRose"}
                                :toggle (j/cell= (not= "" selected-rolon))
                                :click (fn []
                                         (add-output! "> clear rolon selection" command-style)
                                         (reset! selected-rolon ""))
                                "clear rolon selection")
                              )

                            (h/div
                              (h/span
                                (h/strong "Alternate Rolon: "))
                              (h/span
                                :style (j/cell= (if (= "" alternate-rolon)
                                                  ""
                                                  "color:green;cursor:pointer"
                                                  ))
                                :click #(rolon-click @my-ark-record @alternate-rolon)
                                (h/text
                                  (if (= "" alternate-rolon)
                                    "none"
                                    (pretty-uuid my-ark-record (suuid/create-uuid alternate-rolon))))))

                            (h/div
                              :css {:display "none"}
                              :toggle (j/cell= (not= "" alternate-rolon))

                              (h/button
                                :css {:display "none" :background-color "MistyRose"}
                                :toggle (j/cell= (not= "" alternate-rolon))
                                :click (fn []
                                         (add-output! "> clear alternate selection" command-style)
                                         (reset! alternate-rolon ""))
                                "clear alternate selection")
                              )

                            (h/hr)

                            (h/div

                              (h/output (h/strong "Rolon Counts: "))

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (application-rolons-count @my-ark-record))
                                "applications")

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (indexes-count @my-ark-record))
                                "indexes")

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (je-count @my-ark-record))
                                "journal entries"))

                            (h/div
                              :css {:display "none"}
                              :toggle (j/cell= (= "" selected-time))

                              (h/output (h/strong "Transactions: "))

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (add-output! "> Hello Fred transaction" command-style)
                                         (fred))
                                :href ""
                                "Hello Fred")

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (add-output! "> Make Bob transaction" command-style)
                                         (make-bob))
                                :href ""
                                "Make Bob")

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (add-output! "> Invalid!" command-style)
                                         (tiples/chsk-send! [:console/process-transaction {:tran-keyword :invalid :tran-data ""}]))
                                "Invalid!")

                              (h/button
                                :style "background-color:MistyRose"
                                :click (fn []
                                         (add-output! "> Trouble!" command-style)
                                         (tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}]))
                                "Trouble!"))

                            (h/div
                              :style "color:red"
                              (h/p (h/text (if transaction-error
                                             (str "Error: " transaction-error-msg)
                                             "")))
                              )
                            ))

               (h/td :style (j/cell= (td-style login/windowInnerWidth))
                     (h/div :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
                            (h/div :style "white-space:pre-wrap; font-family:monospace"
                                   (h/for-tpl [[txt-id txt style on-click arg] output]
                                              (h/output :id txt-id
                                                        :style style
                                                        :click (fn [] (@on-click  @my-ark-record @arg))
                                                        txt))))
                     )))))

(defmethod login/add-body-element :console [_]
  (do-console))
