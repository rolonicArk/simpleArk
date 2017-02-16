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
    [simpleArk.builder :as builder]
    [cljs-time.core :as time]
    [cljs-time.coerce :as cotime]
    [cljs-time.format :as ftime]
    [cljs.reader :as reader]))

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

(def form-name (j/cell "none"))

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

(def composition (j/cell [{} []]))

(def local (j/cell=
             (first composition)
             (fn [new-local]
               (swap!
                 composition
                 (fn [old-composition]
                   [new-local (second old-composition)])))))

(def actions (j/cell=
               (second composition)
               (fn [new-actions]
                 (swap!
                   composition
                   (fn [old-composition]
                     [(first old-composition) new-actions])))))

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
  [value]
  (cond
    (instance? suuid/Timestamp value)
    clickable-je-style
    (suuid/journal-entry-uuid? value)
    clickable-je-style
    (suuid/index-uuid? value)
    clickable-index-style
    (suuid/random-uuid? value)
    clickable-application-style
    :else
    default-style
    ))

(defn no-click [arg])

(defn scroll-history
  [nw]
  (h/with-timeout
    0
    (let [a (.getElementById js/document (str "ahis" (- (count nw) 1)))
          b (.getElementById js/document (str "bhis" (- (count nw) 1)))]
      (if (some? a)
        (.scrollIntoView a true))
      (if (some? b)
        (.scrollIntoView b true))))
  nw)

(defn add-display
  ([display txt] (add-display display txt default-style))
  ([display txt style] (add-display display txt style no-click nil))
  ([display txt style on-click arg]
   (conj display [txt (style) on-click arg])))

(defn display-history!
  [display]
  (scroll-history
    (swap! history
           (fn [old]
             (reduce
               (fn [old item]
                 (conj old (conj item (str "his" (count old)))))
               old display)))))

(defn add-history!
  ([txt] (add-history! txt default-style))
  ([txt style] (add-history! txt style no-click nil))
  ([txt style on-click arg]
   (display-history! (add-display [] txt style on-click arg))))

(defn display-mode-change
  [_ _ _ _]
  (scroll-history @history))

(add-watch display-mode :display-mode display-mode-change)

(defn clear-output!
  []
  (reset! output []))

(defn scroll-output
  []
  (h/with-timeout
    0
    (let [a (.getElementById js/document (str "aout" 0))
          b (.getElementById js/document (str "bout" 0))]
      (if (some? a)
        (.scrollIntoView a true))
      (if (some? b)
        (.scrollIntoView b true))
      )))

(defn display-output!
  [display]
  (swap! output
         (fn [old]
           (reduce
             (fn [old item]
               (conj old (conj item (str "out" (count old)))))
             old display))
         (scroll-output)))

(defn add-output!
  ([txt] (add-output! txt default-style))
  ([txt style] (add-output! txt style no-click nil))
  ([txt style on-click arg]
   (display-output! (add-display [] txt style on-click arg))))

(defn clickable? [value]
  (or
    (uuid? value)
    (instance? suuid/Timestamp value)))

(defn pretty-value
  [ark-record value]
  (if (uuid? value)
    (cond
      (suuid/journal-entry-uuid? value)
      (ftime/unparse format-date-time (local-date-time value))
      (suuid/random-uuid? value)
      (let [name (arkRecord/get-property-value ark-record value [:index/name])]
        (if name name "_"))
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

(declare uuid-click!)

(defn add-prompt
  [display]
  (let [t (prompt-time)]
    (if t
      (add-display display t clickable-je-style uuid-click! (str (ark-time)))
      display)))

(defn add-prompt!
  []
  (display-history! (add-prompt [])))

(defn micro-property-style [] "color:chocolate;cursor:pointer")

(declare history-path! display-path output-path! explore!)

(defn micro-property-click [ark-record arg]
  (if (< 1 @display-mode)
    (reset! display-mode 1))
  (reset! selected-path arg)
  (display-history!
    (-> []
        (add-prompt)
        (add-display " ")
        (add-display "selected micro-property:" selection-style)
        (add-display " ")
        (display-path ark-record arg)
        (add-display "\n")))
  (explore! ark-record (suuid/create-uuid @selected-rolon) arg))

(defn red [] "color:red")

(defn display-value
  [display ark-record pval]
  (add-display
    display
    (pretty-value ark-record pval)
    (clickable-styles pval)
    uuid-click!
    (str pval))
  )

(defn history-value!
  [ark-record pval]
  (display-history!
    (display-value [] ark-record pval)))

(defn output-value!
  [ark-record pval]
  (display-output!
    (display-value [] ark-record pval)))

(defn display-local
  [display ark-record local]
  (reduce
    (fn [display e]
      (let [k (key e)
            v (val e)]
        (-> display
            (add-display (str k " "))
            (display-value ark-record v)
            (add-display "\n"))))
    display
    local))

(defn display-actions
  [display ark-record actions]
  (first
    (reduce
      (fn [[display line-nbr] a]
        [(-> display
             (add-display (str line-nbr ": "))
             (add-display (builder/pretty-action a))
             (add-display "\n"))
         (+ 1 line-nbr)])
      [display 1]
      actions)))

(defn display-tran
  [display ark-record tran]
  (let [[local actions] tran]
    (-> display
        (add-display "parameters:\n")
        (display-local ark-record (into (sorted-map) local))
        (add-display "\n   actions:\n")
        (display-actions ark-record actions))))

(defn output-tran!
  [ark-record tran]
  (display-output! (display-tran [] ark-record tran)))

(defn display-property
  [display ark-record path pval]
  (display-path display ark-record path)
  (add-display display " = ")
  (let [kw (first path)]
    (if (= kw :edn-transaction/transaction-argument)
      (display-tran display ark-record (reader/read-string pval))
      (display-value display ark-record pval))))

(defn output-property!
  [ark-record path pval]
  (display-output! (display-property [] ark-record path pval)))

(defn explore!
  [ark-record uuid path]
  (if (= "" @selected-rolon)
    (add-history! "No Rolon selected." red)
    (do
      (display-history!
        (-> []
            (add-prompt)
            (add-display ">")
            (add-display "explore " command-prefix-style)
            (display-path ark-record path)
            (add-display " in ")
            (add-display
              (pretty-value ark-record uuid)
              (clickable-styles uuid)
              uuid-click!
              @selected-rolon)
            (add-display "\n")))
      (clear-output!)
      (let [display
            (-> []
                (add-display "explore ")
                (display-path ark-record path)
                (add-display " in ")
                (add-display
                  (pretty-value ark-record uuid)
                  (clickable-styles uuid)
                  uuid-click!
                  @selected-rolon)
                (add-display "\n"))
            ptree (arkRecord/get-property-tree ark-record uuid path)
            pm (first ptree)
            pval (if (= 0 (count path))
                   nil
                   (arkRecord/get-property-value ark-record uuid path))
            display (if (some? pval)
                      (-> display
                          (add-display "\n   ")
                          (display-property ark-record path pval))
                      display)
            display
            (if (some? pm)
              (reduce
                (fn [display e]
                  (let [k (key e)
                        e-path (into path k)
                        pt (val e)
                        count (if (vector? pt)
                                (arkRecord/tree-count ark-record uuid e-path pt)
                                (arkRecord/tree-count ark-record uuid e-path e))
                        display
                        (if (< 0 count)
                          (-> display
                              (add-display "\n")
                              (add-display "=" micro-property-style micro-property-click e-path)
                              (add-display " ")
                              (display-path ark-record e-path)
                              (add-display (str " : " count)))
                          display)]
                    display))
                display
                pm)
              display)
            display (if (some? pm)
                      (add-display display (str "\n\ntotal: " (arkRecord/tree-count ark-record uuid path ptree)))
                      display)]
        (display-output! display)))))

(defn rolon-click! [ark-record arg]
  (reset! display-mode 0)
  (reset! selected-rolon arg)
  (let [uuid (suuid/create-uuid arg)
        display (add-prompt [])
        display (add-display display " ")
        display (if (= "" arg)
                  (add-display display "cleared selected rolon\n" selection-style)
                  (-> display
                      (add-display "selected rolon:" selection-style)
                      (add-display " ")
                      (add-display (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click! arg)))]
    (display-history! display)
    (reset! selected-path [])
    (explore! ark-record uuid [])))

(defn display-index
  [display ark-record content-index index-uuid]
  (let [name (arkRecord/get-property-value ark-record index-uuid [:index/index.name])]
    (reduce (fn [display kv]
              (let [k (str (key kv) " ")
                    v (val kv)
                    display (case name
                              "index.name" display
                              "name" display
                              (add-display display k bold-style))
                    display (add-display display (pretty-value ark-record v)
                                         (clickable-styles v) uuid-click! (str v))
                    display (if (not= name "headline")
                              (let [headline (arkRecord/get-property-value
                                               ark-record
                                               (suuid/create-uuid v)
                                               [:index/headline])]
                                (if (some? headline)
                                  (add-display display (str " - " headline))
                                  display))
                              display)]
                (add-display display "\n")))
            display
            content-index)))

(defn list-index-content! [ark-record index-uuid]
  (add-prompt!)
  (add-history! ">")
  (add-history! "list index content\n" command-prefix-style)
  (clear-output!)
  (add-output! "index: ")
  (add-output! (str (pretty-value ark-record index-uuid) "\n")
               (clickable-styles index-uuid)
               uuid-click!
               (str index-uuid))
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-output!
      (display-index [] ark-record content-index index-uuid))))

(defn uuid-click! [ark-record arg]
  (if (< 1 @display-mode)
    (reset! display-mode 1))
  (let [uuid (suuid/create-uuid arg)]
    (cond
      (suuid/index-uuid? uuid)
      (do
        (reset! selected-index arg)
        (add-prompt!)
        (add-history! " ")
        (add-history! "selected index:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click! arg)
        (list-index-content! ark-record uuid))
      (suuid/journal-entry-uuid? uuid)
      (do
        (add-prompt!)
        (add-history! " ")
        (add-history! "selected time:" selection-style)
        (add-history! " ")
        (add-history! (str (pretty-value ark-record uuid) "\n") (clickable-styles uuid) uuid-click! arg)
        (reset! old-ark-record (:console @login/common-data))
        (reset! selected-time arg))
      (suuid/random-uuid? uuid)
      (rolon-click! ark-record arg))))

(defn my-ark-record-updated! [_ _ _ n]
  (if (and (= "" @selected-time)
           (not (identical? n @old-ark-record)))
    (add-history! "***ark updated***" event-style)))

(add-watch my-ark-record :my-ark-record my-ark-record-updated!)

(defn display-path
  [display ark-record path]
  (let [display (add-display display "[")
        [display _] (reduce
                      (fn [[display space] k]
                        (let [display (if space
                                        (add-display display ", ")
                                        display)
                              display
                              (if (clickable? k)
                                (if (instance? suuid/Timestamp k)
                                  (add-display display
                                               (pretty-value ark-record k)
                                               (clickable-styles k)
                                               uuid-click!
                                               (str (arkRecord/get-journal-entry-uuid ark-record k)))
                                  (add-display display
                                               (pretty-value ark-record k)
                                               (clickable-styles k)
                                               uuid-click!
                                               (str k)))
                                (add-display display (pr-str k)))]
                          [display true]))
                      [display false]
                      path)]
    (add-display display "]")))

(defn output-path!
  [ark-record path]
  (display-output! (display-path [] ark-record path)))

(defn history-path!
  [ark-record path]
  (display-history! (display-path [] ark-record path)))

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
    (if (clickable? k)
      (pretty-value @my-ark-record k)
      (pr-str k))))

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

(defn clear-error!
  []
  (reset! transaction-error false)
  (reset! transaction-error-msg ""))

(defn set-error!
  [m]
  (reset! transaction-error true)
  (reset! transaction-error-msg m))

(defn error!
  [f m]
  (if f
    (set-error! m)
    (clear-error!))
  (not f))

(defn output-composition!
  []
  (reset! display-mode 0)
  (clear-output!)
  (add-output! "Composed Transaction")
  (output-tran! @my-ark-record @composition))

(defn reader
  [edn-string]
  (try
    (let [v (reader/read-string edn-string)]
      (clear-error!)
      v)
    (catch :default e
      (set-error! (str "Unable to read " edn-string))
      (throw e))))

(defn read-cell
  [cell]
  (reader @cell))

(defn valid-parameter
  [name]
  (and
    (not= "" name)
    (not= "je" name)
    (try
      (keyword "local" name)
      true
      (catch :default e
        false))))
