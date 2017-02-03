(ns simpleArk.arkRecord
  (:require #?(:clj
                [simpleArk.reader :as reader]
               :cljs
               [cljs.reader :as reader])
                [simpleArk.miView :as miView]
                [simpleArk.mapish :as mapish]
                [simpleArk.uuid :as suuid]
                [simpleArk.rolonRecord :as rolonRecord]))

#?(:clj
   (set! *warn-on-reflection* true))

(defrecord Ark-record [])

(defn load-ark [m]
  (into (->Ark-record) m))

#?(:clj
   (defn register
     [component-map]
     (reader/register-tag-parser! component-map 'simpleArk.arkRecord.Ark-record load-ark))
   :cljs
   (reader/register-tag-parser! "simpleArk.arkRecord.Ark-record" load-ark))

(defn get-selected-je-uuid [ark-record]
  (:selected-time ark-record))

(defn get-selected-time [ark-record]
  (suuid/rolon-key (get-selected-je-uuid ark-record)))

(defn get-latest-journal-entry-uuid
  [ark-record]
  (:latest-journal-entry-uuid ark-record))

(defn get-journal-entries
  [ark-record]
  (mapish/mi-sub (:journal-entries ark-record) nil nil <= (get-selected-time ark-record)))

(defn get-indexes
  [ark-record]
  (:indexes ark-record))

(defn get-application-rolons
  [ark-record]
  (:random-rolons ark-record))

(defn select-time
  [ark-record je-uuid]
  (let [jes
        (mapish/mi-sub
          (get-journal-entries ark-record)
          nil
          nil
          <=
          [(suuid/rolon-key je-uuid)])
        je-uuid
        (key
          (first
            (rseq jes)))]
    (assoc ark-record :selected-time je-uuid)))

(defn get-rolon
  [ark-record uuid]
  (cond
    (suuid/journal-entry-uuid? uuid) (get (get-journal-entries ark-record)
                                          [(suuid/rolon-key uuid)])
    (suuid/index-uuid? uuid) (get (get-indexes ark-record)
                                  [uuid])
    (suuid/random-uuid? uuid) (get (get-application-rolons ark-record)
                                   [uuid])
    :else
    (get (get-journal-entries ark-record) [uuid])))

(defn get-journal-entry [ark-record timestamp]
  (get (get-journal-entries ark-record) [timestamp]))

(defn get-journal-entry-uuid [ark-record timestamp]
  (rolonRecord/get-rolon-uuid (get-journal-entry ark-record timestamp)))

(defn get-changes-by-property
  ([ark-record rolon-uuid property-path]
   (let [changes-by-property (get-changes-by-property ark-record rolon-uuid)
         pc (get changes-by-property property-path)]
     (if (nil? pc)
       nil
       (mapish/mi-sub pc nil nil <= (get-selected-time ark-record)))))
  ([ark-record rolon-uuid]
   (:changes-by-property (get-rolon ark-record rolon-uuid))))

(defn get-property-tree
  ([ark-record rolon-uuid path]
   (loop [ptree (get-property-tree ark-record rolon-uuid)
          pth path]
     (if (empty? pth)
       ptree
       (let [m (first ptree)
             k [(first pth)]
             subtree (get m k)]
         (if (= true subtree)
           nil
           (recur subtree (next pth)))))))
  ([ark-record rolon-uuid]
   (:property-tree (get-rolon ark-record rolon-uuid))))

(defn tree-count
  ([ark-record rolon-uuid path]
   (tree-count ark-record (get-property-tree ark-record rolon-uuid path)))
  ([ark-record pt]
   (let [tsm (second pt)]
     (if (or (nil? tsm) (= true tsm))
       1
       (let [tsm (mapish/mi-sub tsm nil nil <= (get-selected-time ark-record))]
         (second (first (rseq tsm))))))))

(defn get-property-value
  [ark-record rolon-uuid property-path]
  (mapish/validate-property-path property-path)
  (let [changes (get-changes-by-property ark-record rolon-uuid property-path)]
    (if changes
      (val (first (rseq (mapish/mi-sub changes nil nil <= (get-selected-time ark-record)))))
      nil)))

(defn get-property-values
  ([ark-record rolon-uuid]
   (get-property-values ark-record rolon-uuid (get-changes-by-property ark-record rolon-uuid)))
  ([ark-record rolon-uuid all-changes]
   (miView/->MI-view ark-record rolon-uuid all-changes (get-selected-time ark-record))))

(defn get-link-value
  ([ark-record rolon-uuid link-kw]
   (get-link-value ark-record rolon-uuid link-kw nil))
  ([ark-record rolon-uuid link-kw label]
   (let [prefix (if (nil? label)
                  [link-kw]
                  [link-kw label])
         property-values (get-property-values ark-record rolon-uuid)
         link-properties (mapish/mi-sub property-values prefix)
         lps (seq link-properties)
         link-property (first lps)
         path (key link-property)]
     (if (nil? (next lps))
               (if (nil? label)
                 (nth path 1)
                 (nth path 2))
               #?(:clj (throw (Exception. (str "multiple values for prefix " prefix)))
                  :cljs (throw (str "multiple values for prefix " prefix)))))))

(defn index-lookup
  "returns the uuids for a given index-uuid and value"
  [ark-record index-uuid value]
  (let [property-values (get-property-values ark-record index-uuid)]
    (map
      (fn [e]
        ((key e) 2))
      (filter
        #(some? (val %))
        (seq (mapish/mi-sub
               property-values
               [:content/index value]))))))

(def index-name-uuid-string "8cacc5db-70b3-5a83-85cf-c29541e14114")

(def index-name-uuid (suuid/create-uuid index-name-uuid-string))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-record index-name]
  (first (index-lookup ark-record index-name-uuid index-name)))

(defn name-lookup
  [ark-record rolon-name]
  (let [name-index-uuid (get-index-uuid ark-record "name")]
    (index-lookup ark-record name-index-uuid rolon-name)))

(defn get-content-index
  "returns a seq of [value uuid]"
  [ark-record index-uuid]
  (let [property-values (get-property-values ark-record index-uuid)]
    (if (nil? (first property-values))
      nil
      (map
        (fn [e]
          (let [v (key e)]
            [(v 1) (v 2)]))
        (filter
          #(some? (val %))
          (seq (mapish/mi-sub
                 property-values
                 [:content/index])))))))

(defn get-related-uuids
  "returns a lazy seq of the related uuids"
  [ark-record uuid relation-keyword]
  (map
    (fn [e]
      ((key e) 1))
    (filter
      #(val %)
      (seq (mapish/mi-sub (get-property-values ark-record uuid) [relation-keyword])))))
