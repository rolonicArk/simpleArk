(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as suuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.mapish :as mapish]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.arkRecord :as arkRecord]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-db & keyvals]
  (apply (:ark-value/create-mi ark-db) ark-db keyvals))

(defn ark-record-assoc-mapish
  [ark-record ark-db key]
  (let [mi (create-mi ark-db)]
    (assoc ark-record key mi)))

(defn init-ark-record
  [ark-record ark-db]
  (-> ark-record
      (ark-record-assoc-mapish ark-db :journal-entries)
      (ark-record-assoc-mapish ark-db :indexes)
      (ark-record-assoc-mapish ark-db :random-rolons)))

(defn create-ark
  [ark-db]
  (-> (arkRecord/->Ark-record)
      (init-ark-record ark-db)))

(defn assoc-rolon
  "update the ark with the revised/new rolon"
  [ark-record rolon-uuid rolon-record]
  (cond
    (suuid/journal-entry-uuid? rolon-uuid)
    (let [journal-entries (arkRecord/get-journal-entries ark-record)
          journal-entries (assoc journal-entries [(suuid/rolon-key rolon-uuid)] rolon-record)]
      (assoc ark-record :journal-entries journal-entries))
    (suuid/index-uuid? rolon-uuid)
    (let [indexes (arkRecord/get-indexes ark-record)
          indexes (assoc indexes [rolon-uuid] rolon-record)]
      (assoc ark-record :indexes indexes))
    (suuid/random-uuid? rolon-uuid)
    (let [rolons (arkRecord/get-application-rolons ark-record)
          rolons (assoc rolons [rolon-uuid] rolon-record)]
      (assoc ark-record :random-rolons rolons))
    :else (throw (Exception. (str rolon-uuid " is unrecognized")))))

(defn update-ptree
  [ptree ark-db timestamp-path path add]
  (if (= 1 (count path))
    (if (nil? ptree)
      (do
        [(create-mi ark-db [(first path)] true) (create-mi ark-db timestamp-path 1)])
      (let [count (val (first (rseq (second ptree))))
            count (if add
                    (+ count 1)
                    (- count 1))]
        [(assoc (first ptree) [(first path)] true) (assoc (second ptree) timestamp-path count)]))
    (if (nil? ptree)
      (let [rem-path (rest path)
            sub-tree (update-ptree nil ark-db timestamp-path rem-path add)]
        [(create-mi ark-db [(first path)] sub-tree) (create-mi ark-db timestamp-path 1)])
      (let [rem-path (rest path)
            sub-tree (update-ptree (get (first ptree) [(first path)]) ark-db timestamp-path rem-path add)
            count (val (first (rseq (second ptree))))
            count (if add
                    (+ count 1)
                    (- count 1))]
        [(assoc (first ptree) [(first path)] sub-tree) (assoc (second ptree) timestamp-path count)]))))

(defn update-changes-for-property
  [changes-by-property property-tree ark-db je-uuid path new-value]
  (let [changes-by-property (if (some? changes-by-property)
                              changes-by-property
                              (create-mi ark-db))
        property-changes (get changes-by-property path)
        property-changes (if (some? property-changes)
                           property-changes
                           (create-mi ark-db))
        first-entry (first (seq property-changes))
        old-value (if (nil? first-entry)
                    nil
                    (val first-entry))
        timestamp-path [(suuid/rolon-key je-uuid)]
        changes-by-property (assoc changes-by-property
                              path
                              (if (= new-value old-value)
                                property-changes
                                (assoc property-changes timestamp-path new-value)))
        property-tree (if (= (nil? old-value) (nil? new-value))
                        property-tree
                        (let [upt (update-ptree property-tree ark-db timestamp-path path (some? new-value))]
                          upt))]
    [changes-by-property property-tree]))

(defn update-rolon-properties
  [rolon-record ark-record ark-db je-uuid properties]
  (let [rolon-uuid (rolonRecord/get-rolon-uuid rolon-record)
        changes (arkRecord/get-changes-by-property ark-record rolon-uuid)
        ptree (arkRecord/get-property-tree ark-record rolon-uuid [])
        [changes ptree] (reduce
                          (fn [[ch pt] pe]
                            (update-changes-for-property ch pt ark-db je-uuid (key pe) (val pe)))
                          [changes ptree]
                          (seq properties))
        rolon-record (assoc rolon-record :changes-by-property changes)
        rolon-record (assoc rolon-record :property-tree ptree)]
    rolon-record))

(declare update-properties- je-modified)

(defn update-properties
  [ark-record ark-db rolon-uuid properties]
  (mapish/validate-properties properties)
  (let [journal-entry-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)
        ark-record (update-properties- ark-record ark-db journal-entry-uuid rolon-uuid properties)]
    (je-modified ark-record ark-db rolon-uuid)))

(defn update-property
  [ark-record ark-db rolon-uuid property-path property-value]
  (mapish/validate-property-path property-path)
  (update-properties ark-record ark-db rolon-uuid (create-mi ark-db property-path property-value)))

(defn make-rolon
  [ark-record ark-db rolon-uuid properties]
  (mapish/validate-properties properties)
  (let [ark-record
        (if (arkRecord/get-rolon ark-record rolon-uuid)
          ark-record
          (assoc-rolon
            ark-record
            rolon-uuid
            (rolonRecord/->Rolon-record rolon-uuid)))]
    (update-properties ark-record ark-db rolon-uuid properties)))

(defn make-index-rolon-
  [ark-record ark-db index-keyword value uuid adding]
  (let [iuuid (suuid/index-uuid ark-db index-keyword)
        ark-record (if (arkRecord/get-rolon ark-record iuuid)
                     ark-record
                     (make-rolon ark-record ark-db iuuid
                                 (create-mi
                                   ark-db
                                   [:index/index.name] (name index-keyword))))]
    (update-property ark-record
                     ark-db
                     iuuid
                     [:content/index value uuid]
                     adding)))

(defn make-index-rolons
  "create/update an index rolon"
  [ark-record ark-db uuid properties old-properties]
  (reduce #(let [ark-record %1
                 path (key %2)
                 k (first path)
                 nv (val %2)
                 ov (get old-properties path)
                 ark-record (if (and ov (mapish/index? k))
                              (make-index-rolon- ark-record ark-db k ov uuid nil)
                              ark-record)
                 ark-record (if (and nv (mapish/index? k))
                              (make-index-rolon- ark-record ark-db k nv uuid true)
                              ark-record)]
             ark-record)
          ark-record (seq properties)))

(defn update-properties-
  [ark-record ark-db journal-entry-uuid rolon-uuid properties]
  (let [property-values (arkRecord/get-property-values ark-record rolon-uuid)
        ark-record (make-index-rolons ark-record
                                      ark-db
                                      rolon-uuid
                                      properties
                                      property-values)
        rolon (arkRecord/get-rolon ark-record rolon-uuid)
        rolon (update-rolon-properties rolon ark-record ark-db journal-entry-uuid properties)]
    (assoc-rolon ark-record rolon-uuid rolon)))

(defn update-property-
  [ark-record ark-db journal-entry-uuid rolon-uuid property-path property-value]
  (update-properties- ark-record
                      ark-db
                      journal-entry-uuid
                      rolon-uuid
                      (create-mi ark-db property-path property-value)))

(defn update-relation-
  ([ark-record ark-db relaton-name from-uuid to-uuid symetrical value]
   (update-relation- ark-record ark-db relaton-name nil from-uuid to-uuid symetrical value))
  ([ark-record ark-db relation-name label from-uuid to-uuid symetrical value]
   (let [[rel irel] (if symetrical
                      [(keyword "bi-rel" relation-name) (keyword "bi-rel" relation-name)]
                      [(keyword "rel" relation-name) (keyword "inv-rel" relation-name)])
         from-path (if (some? label)
                     [rel (suuid/rolon-key label) to-uuid]
                     [rel to-uuid])
         to-path (if (some? label)
                   [irel (suuid/rolon-key label) from-uuid]
                   [irel from-uuid])
         journal-entry-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)
         ark-record (update-property- ark-record
                                      ark-db
                                      journal-entry-uuid
                                      from-uuid
                                      from-path
                                      value)]
     (update-property- ark-record
                       ark-db
                       journal-entry-uuid
                       to-uuid
                       to-path
                       value))))

(defn update-relation
  ([ark-record ark-db relaton-name from-uuid to-uuid symetrical value]
   (update-relation ark-record ark-db relaton-name nil from-uuid to-uuid symetrical value))
  ([ark-record ark-db relation-name label from-uuid to-uuid symetrical value]
   (-> ark-record
       (update-relation- ark-db relation-name label from-uuid to-uuid symetrical value)
       (je-modified  ark-db from-uuid)
       (je-modified ark-db to-uuid))))

(defn je-modified
  "track the rolons modified by the journal entry"
  [ark-record ark-db rolon-uuid]
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)]
    (if (= je-uuid rolon-uuid)
      ark-record
      (update-relation- ark-record ark-db "modified" je-uuid rolon-uuid false true))))

(defn destroy-rolon
  [ark-record ark-db rolon-uuid]
  (let [old-property-values (arkRecord/get-property-values ark-record rolon-uuid)
        property-values (reduce #(assoc %1 (key %2) nil)
                                (create-mi ark-db)
                                (seq old-property-values))
        ark-record (make-index-rolons ark-record
                                      ark-db
                                      rolon-uuid
                                      property-values
                                      old-property-values)
        je-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)
        rolon (arkRecord/get-rolon ark-record rolon-uuid)
        rolon (update-rolon-properties rolon ark-record ark-db je-uuid property-values)
        ark-record (assoc-rolon ark-record rolon-uuid rolon)]
    (je-modified ark-record ark-db rolon-uuid)))

(defmulti eval-transaction (fn [ark-record ark-db n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-record ark-db n s]
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)
        [rolon-uuid je-properties-map rolon-properties-map] (read-string s)
        je-properties (create-mi ark-db [:index/headline] (str "update a rolon with " s))
        je-properties (into je-properties je-properties-map)
        rolon-properties (into (create-mi ark-db) rolon-properties-map)]
    (-> ark-record
        (update-properties ark-db je-uuid je-properties)
        (make-rolon ark-db
                    (if (some? rolon-uuid) rolon-uuid
                                           (suuid/random-uuid ark-db))
                    rolon-properties))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-record ark-db n s]
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)
        [uuid je-properties-map] (read-string s)
        je-properties (create-mi ark-db [:index/headline] (str "destroy rolon " s))
        je-properties (into je-properties je-properties-map)]
    (-> ark-record
        (update-properties ark-db je-uuid je-properties)
        (destroy-rolon ark-db uuid))))

(defn reprocess-trans
  [m seq]
  (reduce (fn [_ [je-uuid transaction-name s _]]
            (ark-db/process-transaction! m je-uuid transaction-name s))
          nil
          seq))
