(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.vecish]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(defn classifier?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "classifier" (namespace kw)))))

(defn descriptor?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "descriptor" (namespace kw)))))

(defn create-ark
  [m]
  "returns a new ark"
  ((:ark-value/create-ark m) m))

(defn get-current-journal-entry-uuid
  [ark-value]
  ((:get-current-journal-entry-uuid ark-value) ark-value))

(defn index-name-uuid
  [ark-value]
  ((:index-name-uuid ark-value) ark-value))

(defrecord Ark-value [this-db get-rolon get-journal-entries get-indexes get-random-rolons
                      make-rolon destroy-rolon update-properties update-ark
                      get-current-journal-entry-uuid
                      select-time get-selected-time index-name-uuid
                      create-mi])

(defrecord Rolon [rolon-uuid get-changes-by-property ark-value])

(defn create-mi
  ([ark-value] ((:create-mi ark-value)))
  ([ark-value sorted-map] ((:create-mi ark-value) sorted-map)))

(defn select-time
  "Sets the ark to the time of the journal entry uuid,
  returns the updated ark-value"
  [ark-value je-uuid]
  ((:select-time ark-value) ark-value je-uuid))

(defn get-selected-time
  "returns the journal entry uuid of the selected time"
  [ark-value]
  ((:get-selected-time ark-value) ark-value))

(defn get-ark-db
  "returns the ark-db"
  [ark-value]
  (:this-db ark-value))

(defn get-journal-entries
  "returns an MI of all the journal entry rolons"
  [ark-value]
  ((:get-journal-entries ark-value) ark-value))

(defn get-indexes
  "returns a sorted map of all the index rolons"
  [ark-value]
  ((:get-indexes ark-value) ark-value))

(defn get-random-rolons
  "returns a map of all the random rolons"
  [ark-value]
  ((:get-random-rolons ark-value) ark-value))

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark-value uuid]
  ((:get-rolon ark-value) ark-value uuid))

(defn ark-str
  [ark-value]
  (let [s (str "\n" :ark "\n"
               "\n" :index-rolons "\n\n" (mapish/mi-seq (get-indexes ark-value)) "\n"
               "\n" :journal-entry-rolons "\n\n" (mapish/mi-seq (get-journal-entries ark-value)) "\n"
               "\n" :random-rolons "\n\n" (get-random-rolons ark-value))]
    s))

(defmethod print-method Ark-value
  [ark writer]
  (print-simple (ark-str ark) writer))

(defn $cvt
  [properties]
  (map
    #(clojure.lang.MapEntry. (first (key %)) (val %))
    (mapish/mi-seq properties)))

(defn validate-property-names
  "properties must be classifiers or descriptors"
  [properties]
  (reduce #(if (not (or (classifier? %2) (descriptor? %2)))
            (throw (Exception. (str %2 " is neither a classifier nor a keyword"))))
          nil (keys (mapish/mi-seq properties))))

(defn $validate-property-paths
  [properties]
  (validate-property-names ($cvt properties)))

(defn make-rolon
  [ark-value rolon-uuid properties]
  (validate-property-names properties)
  ((:make-rolon ark-value) ark-value rolon-uuid properties))

(defn $make-rolon
  [ark-value rolon-uuid properties]
  (make-rolon ark-value rolon-uuid ($cvt properties)))

(defn destroy-rolon
  "deletes all the classifiers of a rolon"
  [ark-value rolon-uuid]
  ((:destroy-rolon ark-value) ark-value rolon-uuid))

(defn update-properties
  "update the properties of a rolon"
  [ark-value rolon-uuid properties]
  (validate-property-names properties)
  ((:update-properties ark-value) ark-value rolon-uuid properties))

(defn $update-properties
  [ark-value rolon-uuid properties]
  (update-properties ark-value rolon-uuid ($cvt properties)))

(defn update-property
  "update the value of a property of a rolon"
  [ark-value rolon-uuid property-name property-value]
  (update-properties ark-value rolon-uuid (create-mi ark-value (sorted-map property-name property-value))))

(defn $update-property
  [ark-value rolon-uuid property-path property-value]
  (update-property ark-value rolon-uuid (first property-path) property-value))

(defn get-rolon-uuid
  "returns the uuid of the rolon"
  [rolon]
  (:rolon-uuid rolon))

(defn get-changes-by-property
  ([ark-value rolon-uuid property-name]
   (let [rolon (get-rolon ark-value rolon-uuid)]
     ((:get-changes-by-property rolon) rolon property-name)))
  ([ark-value rolon-uuid]
  (let [rolon (get-rolon ark-value rolon-uuid)]
    ((:get-changes-by-property rolon) rolon))))

(defn $get-changes-by-property
  ([ark-value rolon-uuid property-path]
   (get-changes-by-property ark-value rolon-uuid (first property-path)))
  ([ark-value rolon-uuid]
   (map
     #(clojure.lang.MapEntry. [(key %)] (val %))
     (get-changes-by-property ark-value rolon-uuid))))

(defn get-property-value
  [ark-value rolon-uuid property-name]
  (let [changes (get-changes-by-property ark-value rolon-uuid property-name)]
    (if changes
      (val (first (mapish/mi-rseq (mapish/mi-sub changes nil nil <= (get-selected-time ark-value)))))
      nil)))

(defn $get-property-value
  [ark-value rolon-uuid property-path]
  (get-property-value ark-value rolon-uuid (first property-path)))

(defn get-property-values
  ([ark-value rolon-uuid]
   (get-property-values ark-value rolon-uuid (get-changes-by-property ark-value rolon-uuid)))
  ([ark-value rolon-uuid all-changes]
   (reify
     mapish/MI
     (mi-get [this property-name default]
       (let [changes (mapish/mi-get all-changes property-name)]
         (if (nil? changes)
           default
           (let [changes (mapish/mi-sub changes nil nil <= (get-selected-time ark-value))
                 fst (first (mapish/mi-rseq changes))]
             (if (nil? fst)
               default
               (val fst))))))

     (mi-get [this property-name]
       (mapish/mi-get this property-name nil))

     (mi-seq [this]
       (map
         #(clojure.lang.MapEntry. (key %) (val (val %)))
         (filter
           #(some? (val %))
           (map
             #(clojure.lang.MapEntry.
               (key %)
               (first (mapish/mi-rseq (mapish/mi-sub (val %) nil nil <= (get-selected-time ark-value)))))
             (mapish/mi-seq all-changes)))))

     (mi-rseq [this]
       (map
         #(clojure.lang.MapEntry. (key %) (val (val %)))
         (filter
           #(some? (val %))
           (map
             #(clojure.lang.MapEntry.
               (key %)
               (first (mapish/mi-rseq (mapish/mi-sub (val %) nil nil <= (get-selected-time ark-value)))))
             (mapish/mi-rseq all-changes)))))

     (mi-sub [this start-test start-key end-test end-key]
       (get-property-values ark-value rolon-uuid (mapish/mi-sub all-changes start-test start-key end-test end-key))))))

(defn $get-property-values
  ([ark-value rolon-uuid]
   (get-property-values ark-value rolon-uuid ($get-changes-by-property ark-value rolon-uuid)))
  ([ark-value rolon-uuid all-changes]
   (reify
     mapish/MI
     (mi-get [this property-path default]
       (let [changes (mapish/mi-get all-changes property-path)]
         (if (nil? changes)
           default
           (let [changes (mapish/mi-sub changes nil nil <= (get-selected-time ark-value))
                 fst (first (mapish/mi-rseq changes))]
             (if (nil? fst)
               default
               (val fst))))))

     (mi-get [this property-path]
       (mapish/mi-get this property-path nil))

     (mi-seq [this]
       (map
         #(clojure.lang.MapEntry. (key %) (val (val %)))
         (filter
           #(some? (val %))
           (map
             #(clojure.lang.MapEntry.
               (key %)
               (first (mapish/mi-rseq (mapish/mi-sub (val %) nil nil <= (get-selected-time ark-value)))))
             (mapish/mi-seq all-changes)))))

     (mi-rseq [this]
       (map
         #(clojure.lang.MapEntry. (key %) (val (val %)))
         (filter
           #(some? (val %))
           (map
             #(clojure.lang.MapEntry.
               (key %)
               (first (mapish/mi-rseq (mapish/mi-sub (val %) nil nil <= (get-selected-time ark-value)))))
             (mapish/mi-rseq all-changes)))))

     (mi-sub [this start-test start-key end-test end-key]
       ($get-property-values ark-value rolon-uuid (mapish/mi-sub all-changes start-test start-key end-test end-key))))))

(defn index-lookup
  "returns the uuids for a given index-uuid and value"
  [ark-value index-uuid value]
  (let [index-map (get-property-value ark-value index-uuid :descriptor/index)]
    (mapish/mi-get index-map value)))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-value index-name]
  (first (index-lookup ark-value (index-name-uuid ark-value) index-name)))

(defn name-lookup
  [ark-value rolon-name]
  (let [name-index-uuid (get-index-uuid ark-value "name")]
    (first (index-lookup ark-value name-index-uuid rolon-name))))

(defn get-updated-rolon-uuids
  "returns a mapish of the uuids of the rolons updated by a journal-entry rolon"
  [ark-value je-uuid]
  (let [updated-rolon-uuids (get-property-value ark-value je-uuid :descriptor/updated-rolon-uuids)]
    (if (nil? updated-rolon-uuids)
      (create-mi ark-value)
      updated-rolon-uuids)))

(defn get-index-descriptor
  "returns a mapish of sets of rolon uuids keyed by classifier value"
  [ark-value je-uuid]
  (let [index (get-property-value ark-value je-uuid :descriptor/index)]
    (if (nil? index)
      (create-mi ark-value)
      index)))

(defn make-index-rolon-
  [ark-value classifier value uuid adding]
  (let [iuuid (uuid/index-uuid (get-ark-db ark-value) classifier)
        properties (if (get-rolon ark-value iuuid)
                     (create-mi ark-value)
                     (create-mi ark-value (sorted-map :classifier/index.name (name classifier))))
        ark-value (make-rolon ark-value iuuid properties)
        index-rolon (get-rolon ark-value iuuid)
        index-descriptor (get-index-descriptor ark-value iuuid)
        value-set (mapish/mi-get index-descriptor value)
        value-set (if value-set value-set #{})
        value-set (if adding
                    (conj value-set uuid)
                    (disj value-set uuid))
        index-descriptor (mapish/mi-assoc index-descriptor value value-set)]
    (update-property ark-value (get-rolon-uuid index-rolon) :descriptor/index index-descriptor)))

(defn make-index-rolon
  "create/update an index rolon"
  [ark-value uuid properties old-properties]
  (reduce #(let [ark-value %1
                 k (key %2)
                 nv (val %2)
                 ov (mapish/mi-get old-properties k)
                 ark-value (if (and ov (classifier? k))
                             (make-index-rolon- ark-value k ov uuid false)
                             ark-value)
                 ark-value (if (and nv (classifier? k))
                             (make-index-rolon- ark-value k nv uuid true)
                             ark-value)]
            ark-value)
          ark-value (mapish/mi-seq properties)))

(defmulti eval-transaction (fn [ark-value n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)
        [rolon-uuid je-properties rolon-properties] (read-string s)
        je-properties (into {:classifier/headline (str "update a rolon with " s)} je-properties)]
    (-> ark-value
        (update-properties je-uuid (create-mi ark-value je-properties))
        (make-rolon rolon-uuid (create-mi ark-value rolon-properties)))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)
        [uuid je-properties] (read-string s)
        je-properties (into {:classifier/headline (str "destroy rolon " s)} je-properties)]
    (-> ark-value
        (update-properties je-uuid (create-mi ark-value je-properties))
        (destroy-rolon uuid))))

(defn reprocess-trans
  [m seq]
  (reduce (fn [_ [je-uuid transaction-name s _]]
            (ark-db/process-transaction! m je-uuid transaction-name s))
          nil
          seq))
