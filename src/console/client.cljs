(ns console.client
  (:require
    [clojure.string :as string]
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

(defn ldt [tm]
  (time/to-default-time-zone (cotime/to-date-time tm)))

(defn local-date-time [uuid]
  (ldt (suuid/get-time uuid)))

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
(def selected-path (j/cell []))

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

(defn pretty-value
  [ark-record value]
  (if (uuid? value)
    (cond
      (suuid/journal-entry-uuid? value)
      (ftime/unparse format-date-time (local-date-time value))
      (suuid/random-uuid? value)
      (let [name (arkRecord/get-property-value ark-record value [:index/name])]
        (if name name ""))
      (suuid/index-uuid? value)
      (let [index-name (arkRecord/get-property-value ark-record value [:index/index.name])]
        (if index-name (str ":index/" index-name) ""))
      :else
      (pr-str value))
    (if (instance? suuid/Timestamp value)
      (let [^suuid/Timestamp timestamp value
            ts (.-value timestamp)
            tm (suuid/posix-time ts)
            ldt (ldt tm)]
        (ftime/unparse format-date-time ldt))
      (pr-str value))))

(declare uuid-click)

(defn add-prompt []
  (let [t (prompt-time)]
    (if t
      (add-history! t clickable-je-style uuid-click (str (ark-time))))))

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
        (add-history! (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)))))

(defn micro-property-style [] "color:chocolate;cursor:pointer")

(declare history-path! output-path! explore)

(defn micro-property-click [ark-record arg]
  (if (< 1 @display-mode)
    (reset! display-mode 1))
  (reset! selected-path arg)
  (add-prompt)
  (add-history! " ")
  (add-history! "selected micro-property:" selection-style)
  (add-history! " ")
  (history-path! ark-record arg)
  (add-history! "\n")
  (explore ark-record (suuid/create-uuid @selected-rolon) arg))

(defn red [] "color:red")

(defn explore
  [ark-record uuid path]
  (if (= "" @selected-rolon)
    (add-history! "No Rolon selected." red)
    (do
      (add-prompt)
      (add-history! ">")
      (add-history! "explore " command-prefix-style)
      (history-path! ark-record path)
      (add-history! " in ")
      (add-history!
        (pretty-value ark-record uuid)
        (clickable-styles uuid)
        uuid-click
        @selected-rolon)
      (add-history! "\n")
      (clear-output!)
      (add-output! "explore ")
      (output-path! ark-record path)
      (add-output! " in ")
      (add-output!
        (pretty-value ark-record uuid)
        (clickable-styles uuid)
        uuid-click
        @selected-rolon)
      (add-output! "\n")
      (let [ptree (arkRecord/get-property-tree ark-record uuid path)
            pm (first ptree)
            pval (if (= 0 (count path))
                   nil
                   (arkRecord/get-property-value ark-record uuid path))]
        (when (some? pval)
          (add-output! "\n   ")
          (output-path! ark-record path)
          (if (uuid? pval)
            (do
              (add-output! " = ")
              (add-output!
                (pretty-value ark-record pval)
                (clickable-styles pval)
                uuid-click
                (str pval)))
            (add-output! (str " = " (pr-str pval)))))
        (when (some? pm)
          (reduce
            (fn [_ e]
              (let [k (key e)
                    e-path (into path k)
                    pt (val e)
                    count (if (vector? pt)
                            (arkRecord/tree-count ark-record pt)
                            (arkRecord/tree-count ark-record e))
                    value (if (not= count 1)
                            nil
                            (arkRecord/get-property-value ark-record uuid e-path))]
                (if (< 0 count)
                  (do
                    (add-output! "\n")
                    (add-output! "=" micro-property-style micro-property-click e-path)
                    (add-output! " ")
                    (output-path! ark-record e-path)
                    (if (nil? value)
                      (add-output! (str " : " count))
                      (if (uuid? value)
                        (do
                          (add-output! " = ")
                          (add-output!
                            (pretty-value ark-record value)
                            (clickable-styles value)
                            uuid-click
                            (str value)))
                        (add-output! (str " = " (pr-str value))))))))
              nil)
            nil
            pm))
        (add-output! (str "\n\ntotal: " (arkRecord/tree-count ark-record ptree)))))))

(defn rolon-click [ark-record arg]
  (let [uuid (suuid/create-uuid arg)]
    (reset! display-mode 0)
    (reset! selected-rolon arg)
    (add-prompt)
    (add-history! " ")
    (if (= "" arg)
      (do
        (add-history! "cleared selected rolon\n" selection-style))
      (do
        (add-history! "selected rolon:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)))
    (reset! selected-path [])
    (explore ark-record uuid [])))

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
                    (add-output! (pretty-value ark-record v)
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
  (add-output! (str (pretty-value ark-record index-uuid) "\n")
               (clickable-styles index-uuid)
               uuid-click
               (str index-uuid))
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-index ark-record content-index index-uuid)))

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
        (add-history! (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)
        (list-index-content ark-record uuid))
      (suuid/journal-entry-uuid? uuid)
      (do
        (add-prompt)
        (add-history! " ")
        (add-history! "selected time:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click arg)
        (reset! old-ark-record (:console @login/common-data))
        (reset! selected-time arg))
      (suuid/random-uuid? uuid)
      (rolon-click ark-record arg))))

(defn my-ark-record-updated [_ _ _ n]
  (if (and (= "" @selected-time)
           (not (identical? n @old-ark-record)))
    (add-history! "***ark updated***" event-style)))

(add-watch my-ark-record :my-ark-record my-ark-record-updated)

(defn display-path [ark-record path add!]
  (let [fk (first path)
        rel (or
              (mapish/bi-rel? fk)
              (mapish/rel? fk)
              (mapish/inv-rel? fk))]
    (add! "[")
    (reduce
      (fn [space k]
        (if space (add! ", "))
        (if (uuid? k)
          (add! (pretty-value ark-record k) (clickable-styles k) uuid-click (str k))
          (if (and space rel)
            (let [je-uuid (arkRecord/get-journal-entry-uuid ark-record k)]
              (add! (pretty-value ark-record je-uuid) (clickable-styles je-uuid) uuid-click (str je-uuid)))
            (add! (pr-str k))))
        true)
      false path)
    (add! "]")))

(defn output-path!
  [ark-record path]
  (display-path ark-record path add-output!))

(defn history-path!
  [ark-record path]
  (display-path ark-record path add-history!))

(defn selected-path-space
  [v]
  (let [[p _] v
        s (< 0 p)]
    (if s ", " "")))

(defn selected-path-pretty
  [ark-record path v]
  (let [[p k] v
        s (< 0 p)
        fk (first path)
        rel (or
              (mapish/bi-rel? fk)
              (mapish/rel? fk)
              (mapish/inv-rel? fk))]
    (if (uuid? k)
      (pretty-value @my-ark-record k)
      (if (and s rel)
        (let [je-uuid (arkRecord/get-journal-entry-uuid ark-record k)]
          (pretty-value ark-record je-uuid))
        (pr-str k)))))

(defn display-selected-path []
  (h/span
    "["
    (h/for-tpl
      [v
       (j/cell=
         (if (= 0 (count selected-path))
           []
           (map
             (fn [i] [i (nth selected-path i)])
             (range (count selected-path)))))]
      (h/span
        (h/text
          (selected-path-space v))
        (h/text
          (selected-path-pretty my-ark-record selected-path v))))
    "]"))
