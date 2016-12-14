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

(defn local-date-time [uuid]
  (let [tm (suuid/get-time uuid)
        dt (cotime/to-date-time tm)]
    (time/to-default-time-zone dt)))

(def format-date-time (ftime/formatters :mysql))
(def format-time (ftime/formatters :hour-minute-second))

(def transaction-je-uuid-string (j/cell nil))
(def transaction-error (j/cell false))
(def transaction-error-msg (j/cell ""))

(def selected-time (j/cell ""))
(def old-ark-record (j/cell nil))

(def my-ark-record
  (j/cell=
    (if login/common-data
      (let [n (:console login/common-data)]
        (if (= "" selected-time)
          n
          (arkRecord/select-time n (suuid/create-uuid selected-time))))
      nil)
    (partial swap! login/common-data assoc :console)))

(def display-mode (j/cell 0))

(def latest-journal-entry-uuid
  (j/cell=
    (arkRecord/get-latest-journal-entry-uuid my-ark-record)))

(defn ark-time []
  (if (= "" @selected-time)
    @latest-journal-entry-uuid
    (suuid/create-uuid @selected-time)))

(defn prompt-time []
  (let [t (ark-time)]
    (if t
      (ftime/unparse format-time (local-date-time t))
      nil)))

(def output (j/cell []))
(def history (j/cell []))

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

(def channel-open (j/cell true))

(defn watch-state [_ _ _ n]
  (reset! channel-open (:open? n)))

(add-watch tiples/chsk-state :open? watch-state)

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

(defn default-style
  []
  "")

(defn bold-style
  []
  "font-weight:bold")

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
  "color:YellowGreen;cursor:pointer")

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

(defn scroll-history
  [nw]
  (let [a (.getElementById js/document (str "ahis" (- (count nw) 1)))
        b (.getElementById js/document (str "bhis" (- (count nw) 1)))]
    (if (some? a)
      (.scrollIntoView a true))
    (if (some? b)
      (.scrollIntoView b true)))
  )

(defn display-mode-change
  [_ _ _ _]
  (h/with-timeout
    0
    (scroll-history @history)))

(defn add-history!
  ([txt] (add-history! txt default-style))
  ([txt style] (add-history! txt style no-click nil))
  ([txt style on-click arg]
   (swap! history (fn [old]
                    (let [v [(str "his" (count old)) txt (style) on-click arg]
                          nw (conj old v)]
                      (h/with-timeout
                        0
                        (scroll-history nw))
                      nw)))))

(defn clear-output!
  []
  (reset! output []))

(defn add-output!
  ([txt] (add-output! txt default-style))
  ([txt style] (add-output! txt style no-click nil))
  ([txt style on-click arg]
   (swap! output (fn [old]
                   (let [v [(str "out" (count old)) txt (style) on-click arg]
                         nw (conj old v)]
                     (h/with-timeout
                       0
                       (let [a (.getElementById js/document (str "aout" 0))
                             b (.getElementById js/document (str "bout" 0))]
                         (if (some? a)
                           (.scrollIntoView a true))
                         (if (some? b)
                           (.scrollIntoView b true))
                         ))
                     nw)))))

(defn pretty-uuid
  [ark-record uuid]
  (cond
    (suuid/journal-entry-uuid? uuid)
    (ftime/unparse format-date-time (local-date-time uuid))
    (suuid/random-uuid? uuid)
    (let [name (arkRecord/get-property-value ark-record uuid [:index/name])]
      (if name name ""))
    (suuid/index-uuid? uuid)
    (let [index-name (arkRecord/get-property-value ark-record uuid [:index/index.name])]
      (if index-name (str ":index/" index-name) ""))
    :else
    (str uuid)))

(declare uuid-click)

(defn add-prompt []
  (let [t (prompt-time)]
    (if t
      (add-history! t clickable-je-style uuid-click (str (ark-time))))))

(defn rolon-click [ark-record arg]
  (let [uuid (suuid/create-uuid arg)]
    (reset! selected-rolon arg)
    (add-prompt)
    (add-history! " ")
    (if (= "" arg)
      (do
        (add-history! "cleared selected rolon\n" selection-style))
      (do
        (add-history! "selected rolon:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-uuid ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)))))

(defn alternate-click [ark-record arg]
  (let [uuid (suuid/create-uuid arg)]
    (reset! alternate-rolon arg)
    (add-prompt)
    (add-history! " ")
    (if (= "" arg)
      (add-history! "cleared alternate rolon\n" selection-style)
      (do
        (add-history! "alternate rolon:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-uuid ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)))))

(defn uuid-click [ark-record arg]
  (if (< 1 @display-mode)
    (reset! display-mode 1))
  (let [uuid (suuid/create-uuid arg)]
    (cond
      (suuid/index-uuid? uuid)
      (do
        (reset! selected-index arg)
        (add-prompt)
        (add-history! " ")
        (add-history! "selected index:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-uuid ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg))
      (suuid/journal-entry-uuid? uuid)
      (do
        (add-prompt)
        (add-history! " ")
        (add-history! "selected time:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-uuid ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)
        (reset! old-ark-record (:console @login/common-data))
        (reset! selected-time arg))
      (suuid/random-uuid? uuid)
      (rolon-click ark-record arg))))

(defn my-ark-record-updated [_ _ _ n]
  (if (and (= "" @selected-time)
           (not (identical? n @old-ark-record)))
    (add-history! "***ark updated***" event-style)))

(add-watch my-ark-record :my-ark-record my-ark-record-updated)

(defn je-count [ark-record]
  (add-prompt)
  (add-history! ">")
  (add-history! "journal entry rolons count:" command-prefix-style)
  (add-history! " ")
  (add-history! (str (count (arkRecord/get-journal-entries ark-record)) "\n")))

(defn indexes-count [ark-record]
  (add-prompt)
  (add-history! ">")
  (add-history! "index rolons count:" command-prefix-style)
  (add-history! " ")
  (add-history! (str (count (arkRecord/get-indexes ark-record)) "\n")))

(defn application-rolons-count [ark-record]
  (add-prompt)
  (add-history! ">")
  (add-history! "application rolons count:" command-prefix-style)
  (add-history! " ")
  (add-history! (str (count (arkRecord/get-application-rolons ark-record)) "\n")))

(defn display-index
  [ark-record content-index index-uuid]
  (let [name (arkRecord/get-property-value ark-record index-uuid [:index/index.name])]
    (doall (map (fn [kv]
                  (let [k (str (key kv) " ")
                        v (val kv)]
                    (case name
                      "index.name" ()
                      "name" ()
                      (add-output! k bold-style))
                    (add-output! (pretty-uuid ark-record v)
                                 (clickable-styles v) uuid-click (str v))
                    (if (not= name "headline")
                      (let [headline (arkRecord/get-property-value
                                       ark-record
                                       (suuid/create-uuid v)
                                       [:index/headline])]
                        (if (some? headline)
                          (add-output! (str " - " headline)))))
                    (add-output! "\n")))
                content-index))))

(defn list-index-content [ark-record index-uuid]
  (add-prompt)
  (add-history! ">")
  (add-history! "list index content\n" command-prefix-style)
  (clear-output!)
  (add-output! "index: ")
  (add-output! (str (pretty-uuid ark-record index-uuid) "\n")
               (clickable-styles index-uuid)
               uuid-click
               (str index-uuid))
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-index ark-record content-index index-uuid)))

(defn format-path [ark-record path]
  (let [fk (first path)
        rel (or
              (mapish/bi-rel? fk)
              (mapish/rel? fk)
              (mapish/inv-rel? fk))]
    (add-output! "[")
    (reduce
      (fn [space k]
        (if space (add-output! " "))
        (if (uuid? k)
          (add-output! (pretty-uuid ark-record k) (clickable-styles k) uuid-click (str k))
          (if (and space rel)
            (let [je-uuid (arkRecord/get-journal-entry-uuid ark-record k)]
              (add-output! (pretty-uuid ark-record je-uuid) (clickable-styles je-uuid) uuid-click (str je-uuid)))
            (add-output! (pr-str k))))
        true)
      false path)
    (add-output! "]")))

(defn list-current-micro-properties [ark-record]
  (add-prompt)
  (add-history! ">")
  (add-history! "list current micro-properties\n" command-prefix-style)
  (clear-output!)
  (add-output! "current micro-properties of ")
  (let [uuid (suuid/create-uuid @selected-rolon)
        properties (arkRecord/get-property-values ark-record uuid)]
    (add-output! (pretty-uuid ark-record uuid) (clickable-styles uuid) uuid-click @selected-rolon)
    (add-output! ":\n\n")
    (reduce
      (fn [_ [path value]]
        (format-path ark-record path)
        (add-output! " ")
        (add-output! (pr-str value))
        (add-output! "\n\n"))
      nil properties))
  )

(defn do-commands
  []
  (h/div
    (h/div
      (h/span
        (h/strong
          "Selected time: "))
      (h/span
        :style (j/cell= (if (= "" selected-time)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(rolon-click @my-ark-record @selected-time)
        (h/text
          (if (= "" selected-time)
            "now"
            (pretty-uuid my-ark-record (suuid/create-uuid selected-time))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" selected-time)
                         (some? (arkRecord/get-property-value
                                  my-ark-record
                                  (suuid/create-uuid selected-time)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" selected-time)
            (arkRecord/get-property-value
              my-ark-record
              (suuid/create-uuid selected-time)
              [:index/headline])))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" selected-time))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "clear time selection\n" command-prefix-style)
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
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(rolon-click @my-ark-record @selected-index)
        (h/text
          (if (= "" selected-index)
            "none"
            (pretty-uuid my-ark-record (suuid/create-uuid selected-index))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" selected-index)
                         (some? (arkRecord/get-property-value
                                  my-ark-record
                                  (suuid/create-uuid selected-index)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" selected-index)
            (arkRecord/get-property-value
              my-ark-record
              (suuid/create-uuid selected-index)
              [:index/headline])))))

    (h/div

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (list-index-content @my-ark-record arkRecord/index-name-uuid))
        "list indexes")

      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" selected-index))
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "clear index selection\n" command-prefix-style)
                 (reset! selected-index ""))
        "clear index selection")

      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" selected-index))
        :click (fn []
                 (reset! display-mode 0)
                 (list-index-content @my-ark-record
                                     (suuid/create-uuid @selected-index)))
        "list index content"))

    (h/hr)

    (h/div
      (h/span
        (h/strong "Selected Rolon: "))
      (h/span
        :style (j/cell= (if (= "" selected-rolon)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(alternate-click @my-ark-record @selected-rolon)
        (h/text
          (if (= "" selected-rolon)
            "none"
            (pretty-uuid my-ark-record (suuid/create-uuid selected-rolon))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" selected-rolon)
                         (some? (arkRecord/get-property-value
                                  my-ark-record
                                  (suuid/create-uuid selected-rolon)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" selected-rolon)
            (arkRecord/get-property-value
              my-ark-record
              (suuid/create-uuid selected-rolon)
              [:index/headline])))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" selected-rolon))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "clear rolon selection\n" command-prefix-style)
                 (reset! selected-rolon ""))
        "clear rolon selection")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (list-current-micro-properties @my-ark-record))
        "list current micro-properties")
      )

    (h/hr)

    (h/div
      (h/span
        (h/strong "Alternate Rolon: "))
      (h/span
        :style (j/cell= (if (= "" alternate-rolon)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(rolon-click @my-ark-record @alternate-rolon)
        (h/text
          (if (= "" alternate-rolon)
            "none"
            (pretty-uuid my-ark-record (suuid/create-uuid alternate-rolon))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" alternate-rolon)
                         (some? (arkRecord/get-property-value
                                  my-ark-record
                                  (suuid/create-uuid alternate-rolon)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" alternate-rolon)
            (arkRecord/get-property-value
              my-ark-record
              (suuid/create-uuid alternate-rolon)
              [:index/headline])))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" alternate-rolon))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "clear alternate selection\n" command-prefix-style)
                 (reset! alternate-rolon ""))
        "clear alternate selection")

      (h/button
        :style "background-color:MistyRose"
        :css {:display "none"}
        :toggle (j/cell= (not= selected-rolon alternate-rolon))
        :click (fn []
                 (reset! display-mode 0)
                 (let [r @selected-rolon
                       a @alternate-rolon]
                   (rolon-click @my-ark-record a)
                   (alternate-click @my-ark-record r)))
        "swap with rolon selection")
      )

    (h/hr)

    (h/div

      (h/output (h/strong "Rolon Counts: "))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (application-rolons-count @my-ark-record))
        "applications")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (indexes-count @my-ark-record))
        "indexes")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (je-count @my-ark-record))
        "journal entries"))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         channel-open
                         (= "" selected-time)))

      (h/output (h/strong "Transactions: "))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "Hello Fred transaction\n" command-prefix-style)
                 (fred))
        :href ""
        "Hello Fred")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "Make Bob transaction\n" command-prefix-style)
                 (make-bob))
        :href ""
        "Make Bob")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "Invalid!\n" command-prefix-style)
                 (tiples/chsk-send! [:console/process-transaction {:tran-keyword :invalid :tran-data ""}]))
        "Invalid!")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! display-mode 0)
                 (add-prompt)
                 (add-history! ">")
                 (add-history! "Trouble!\n" command-prefix-style)
                 (tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}]))
        "Trouble!"))

    (h/div
      :style "color:red"
      (h/p (h/text (if transaction-error
                     (str "Error: " transaction-error-msg)
                     ""))))))

(defn do-history
  [id-prefix]
  (h/div :style "white-space:pre-wrap; font-family:monospace"
         (h/for-tpl [[txt-id txt style on-click arg] history]
                    (h/output
                      :id (j/cell= (str id-prefix txt-id))
                      :style style
                      :click (fn [] (@on-click @my-ark-record @arg))
                      txt))))

(defn do-output
  []
  (h/div
    :style "white-space:pre-wrap; font-family:monospace"
    (h/for-tpl [[txt-id txt style on-click arg] output]
               (h/output
                 :id txt-id
                 :style style
                 :click (fn [] (@on-click @my-ark-record @arg))
                 txt))))

(defn td2-style [width]
  (str "width:" (/ width 2) "px"))

(defn tx-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (- windowInnerHeight header-height 50) "px;vertical-align:bottom")))

(defn tx2-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (quot (- windowInnerHeight header-height 50) 2) "px;vertical-align:bottom")))

(def do-all
  (h/table
    :style "width:100%"
    (h/tr
      (h/td
        :style (j/cell= (td2-style login/windowInnerWidth))
        (h/div
          :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
          (do-commands)))
      (h/td
        :style (j/cell= (td2-style login/windowInnerWidth))
        (h/div
          :style (j/cell= (tx2-style login/windowInnerHeight login/header-height))
          (do-history "a"))
        (h/div
          :style (j/cell= (tx2-style login/windowInnerHeight login/header-height))
          (do-output))))))

(add-watch display-mode :display-mode display-mode-change)

(defmethod login/add-body-element :console [_]
  (h/div
    (h/div
      :css {:display "none"}
      :toggle (j/cell= (= 0 display-mode))
      (do-all))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 1 display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (do-commands)))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 3 display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (do-history "b")))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 4 display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (do-output)))
    ))

(defmethod login/add-header-element :console [_]
  (h/div
    (h/h2 "Ark Console")
    (h/table
      :style "width:100%"
      (h/tr
        (h/td
          :style "width:16%"
          (h/span
            :style "font-weight:bold"
            "connected: ")
          (h/text channel-open))
        (h/td
          :style "width:16%"
          (h/button
            :click (fn []
                     (reset! display-mode 0)
                     (reset! history []))
            "clear history"))
        (h/td
          :style "font-weight:bold; width:16%; text-align: center"
          "views:")
        (h/td
          :style (j/cell= (if (= 0 display-mode)
                          "font-weight:bold; text-align:center"
                          "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! display-mode 0)
          (h/span "composite"))
        (h/td
          :style (j/cell= (if (= 1 display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! display-mode 1)
          (h/span "commands"))
        (h/td
          :style (j/cell= (if (= 3 display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! display-mode 3)
          (h/span "history"))
        (h/td
          :style (j/cell= (if (= 4 display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! display-mode 4)
          (h/span "output"))))))
