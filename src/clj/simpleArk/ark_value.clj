(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.vecish :as vecish]
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

(defn $validate-property-path
  [property-path]
  (if (not (instance? simpleArk.vecish.Vecish property-path))
    (throw (Exception. (str property-path " is not a vecish")))
    (let [kw (first (:v property-path))]
      (if (classifier? kw)
        (if (< 1 (count (:v property-path)))
          (throw (Exception. (str property-path " has too many elements for a classifier"))))
        (if (not (descriptor? kw))
          (throw (Exception. (str property-path " is neither a classifier nor a keyword"))))))))

(defn $validate-property-paths
  [properties]
  (reduce (fn [_ p] ($validate-property-path p))
          nil (keys (mapish/mi-seq properties))))

(defn $make-rolon
  [ark-value rolon-uuid properties]
  ($validate-property-paths properties)
  ((:make-rolon ark-value) ark-value rolon-uuid properties))

(defn destroy-rolon
  "deletes all the classifiers of a rolon"
  [ark-value rolon-uuid]
  ((:destroy-rolon ark-value) ark-value rolon-uuid))

(defn $update-properties
  [ark-value rolon-uuid properties]
  ($validate-property-paths properties)
  ((:update-properties ark-value) ark-value rolon-uuid properties))

(defn $update-property
  [ark-value rolon-uuid property-path property-value]
  ($validate-property-path property-path)
  ($update-properties ark-value rolon-uuid (create-mi ark-value (sorted-map property-path property-value))))

(defn get-rolon-uuid
  "returns the uuid of the rolon"
  [rolon]
  (:rolon-uuid rolon))

(defn $get-changes-by-property
  ([ark-value rolon-uuid property-path]
   ($validate-property-path property-path)
   (let [rolon (get-rolon ark-value rolon-uuid)]
     ((:get-changes-by-property rolon) rolon property-path)))
  ([ark-value rolon-uuid]
   (let [rolon (get-rolon ark-value rolon-uuid)]
     ((:get-changes-by-property rolon) rolon))))

(defn $get-property-value
  [ark-value rolon-uuid property-path]
  ($validate-property-path property-path)
  (let [changes ($get-changes-by-property ark-value rolon-uuid property-path)]
    (if changes
      (val (first (mapish/mi-rseq (mapish/mi-sub changes nil nil <= (get-selected-time ark-value)))))
      nil)))

(defn $get-property-values
  ([ark-value rolon-uuid]
   ($get-property-values ark-value rolon-uuid ($get-changes-by-property ark-value rolon-uuid)))
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

(defn $index-lookup
  "returns the uuids for a given index-uuid and name"
  [ark-value index-uuid name]
  (let [index-map ($get-property-value ark-value index-uuid (vecish/->Vecish [:descriptor/index]))]
    (mapish/mi-get index-map name)))

(defn $get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-value index-name]
  (first ($index-lookup ark-value (index-name-uuid ark-value) index-name)))

(defn $name-lookup
  [ark-value rolon-name]
  (let [name-index-uuid ($get-index-uuid ark-value "name")]
    (first ($index-lookup ark-value name-index-uuid rolon-name))))

(defn $get-updated-rolon-uuids
  "returns a mapish of the uuids of the rolons updated by a journal-entry rolon"
  [ark-value je-uuid]
  (let [updated-rolon-uuids ($get-property-value ark-value
                                                 je-uuid
                                                 (vecish/->Vecish [:descriptor/updated-rolon-uuids]))]
    (if (nil? updated-rolon-uuids)
      (create-mi ark-value)
      updated-rolon-uuids)))

(defn $get-index-descriptor
  "returns a mapish of sets of rolon uuids keyed by classifier value"
  [ark-value je-uuid]
  (let [index ($get-property-value ark-value je-uuid (vecish/->Vecish [:descriptor/index]))]
    (if (nil? index)
      (create-mi ark-value)
      index)))

(defn $make-index-rolon-
  [ark-value classifier value uuid adding]
  (let [iuuid (uuid/index-uuid (get-ark-db ark-value) classifier)
        properties (if (get-rolon ark-value iuuid)
                     (create-mi ark-value)
                     (create-mi ark-value (sorted-map (vecish/->Vecish [:classifier/index.name])
                                                      (name classifier))))
        ark-value ($make-rolon ark-value iuuid properties)
        index-rolon (get-rolon ark-value iuuid)
        index-descriptor ($get-index-descriptor ark-value iuuid)
        value-set (mapish/mi-get index-descriptor value)
        value-set (if value-set value-set #{})
        value-set (if adding
                    (conj value-set uuid)
                    (disj value-set uuid))
        index-descriptor (mapish/mi-assoc index-descriptor value value-set)]
    ($update-property ark-value
                      (get-rolon-uuid index-rolon)
                      (vecish/->Vecish [:descriptor/index])
                      index-descriptor)))

(defn $make-index-rolon
  "create/update an index rolon"
  [ark-value uuid properties old-properties]
  (reduce #(let [ark-value %1
                 path (key %2)
                 k (first (:v path))
                 nv (val %2)
                 ov (mapish/mi-get old-properties path)
                 ark-value (if (and ov (classifier? k))
                             ($make-index-rolon- ark-value k ov uuid false)
                             ark-value)
                 ark-value (if (and nv (classifier? k))
                             ($make-index-rolon- ark-value k nv uuid true)
                             ark-value)]
            ark-value)
          ark-value (mapish/mi-seq properties)))

(defmulti eval-transaction (fn [ark-value n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)
        [rolon-uuid je-properties rolon-properties] (read-string s)
        je-properties (into (sorted-map (vecish/->Vecish [:classifier/headline])
                                        (str "update a rolon with " s))
                            je-properties)]
    (-> ark-value
        ($update-properties je-uuid (create-mi ark-value je-properties))
        ($make-rolon rolon-uuid (create-mi ark-value rolon-properties)))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)
        [uuid je-properties] (read-string s)
        je-properties (into (sorted-map (vecish/->Vecish [:classifier/headline]) (str "destroy rolon " s))
                            je-properties)]
    (-> ark-value
        ($update-properties je-uuid (create-mi ark-value je-properties))
        (destroy-rolon uuid))))

(defn reprocess-trans
  [m seq]
  (reduce (fn [_ [je-uuid transaction-name s _]]
            (ark-db/process-transaction! m je-uuid transaction-name s))
          nil
          seq))
