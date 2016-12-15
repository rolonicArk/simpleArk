(ns console.client
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid :as suuid]
    [simpleArk.miMap]
    [simpleArk.rolonRecord]
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

(add-watch display-mode :display-mode display-mode-change)

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
  (add-history! "transactions count:" command-prefix-style)
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

(defn list-current-micro-properties
  [ark-record]
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
      nil properties)))

(defn list-modifying-transactions
  [ark-record]
  (add-prompt)
  (add-history! ">")
  (add-history! "list modifying transactions\n" command-prefix-style)
  (clear-output!)
  (add-output! "transactions that modifified ")
  (let [uuid (suuid/create-uuid @selected-rolon)
        all-properties (arkRecord/get-property-values ark-record uuid)
        properties (mapish/mi-sub all-properties [:inv-rel/modified])]
    (add-output! (pretty-uuid ark-record uuid) (clickable-styles uuid) uuid-click @selected-rolon)
    (add-output! "\n\n")
    (.log js/console (pr-str properties))
    (reduce
      (fn [_ [path value]]
        (let [k (nth path 1)
              u (arkRecord/get-journal-entry-uuid ark-record k)]
          (add-output! (pretty-uuid ark-record u) (clickable-styles u) uuid-click (str u))
          (let [headline (arkRecord/get-property-value
                           ark-record
                           u
                           [:index/headline])]
            (if (some? headline)
              (add-output! (str " - " headline)))))
        (add-output! "\n"))
      nil properties)))
