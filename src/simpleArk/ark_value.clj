(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.mapish :as mapish]
            [simpleArk.miView :as miView]
            [simpleArk.rolonRecord :as rolonRecord]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-db & keyvals] (apply (:ark-value/create-mi ark-db) ark-db keyvals))

(defn ark-value-assoc-mapish
  [ark-value ark-db key]
  (let [mi (create-mi ark-db)]
    (assoc ark-value key mi)))

(defn init-ark-value
  [ark-value ark-db]
  (-> ark-value
      (ark-value-assoc-mapish ark-db :journal-entries)
      (ark-value-assoc-mapish ark-db :indexes)
      (ark-value-assoc-mapish ark-db :random-rolons)))

(defn create-ark
  [m]
  "returns a new ark"
  ((:ark-value/create-ark m) m))

(defn get-selected-time [ark-value]
  (:selected-time ark-value))

(defrecord Ark-value [this-db])

(defn index-name-uuid
  [ark-value]
  (uuid/index-uuid (:this-db ark-value) :index/index.name))

(defn get-latest-journal-entry-uuid
  [ark-value]
  (:latest-journal-entry-uuid ark-value))

(defn get-ark-db
  "returns the ark-db"
  [ark-value]
  (:this-db ark-value))

(defn get-journal-entries
  [ark-value]
  (mapish/mi-sub (:journal-entries ark-value) nil nil <= (get-selected-time ark-value)))

(defn get-indexes
  [ark-value]
  (:indexes ark-value))

(defn get-random-rolons
  [ark-value]
  (:random-rolons ark-value))

(defn select-time
  [ark-value je-uuid]
  (let [jes
        (mapish/mi-sub
          (get-journal-entries ark-value)
          nil
          nil
          <=
          [je-uuid])
        je-uuid
        (key
          (first
            (rseq jes)))]
    (assoc ark-value :selected-time je-uuid)))

(defn get-rolon
  [ark-value uuid]
  (cond
    (uuid/journal-entry-uuid? uuid) (get (get-journal-entries ark-value) [uuid])
    (uuid/index-uuid? uuid) (get (get-indexes ark-value) [uuid])
    (uuid/random-uuid? uuid) (get (get-random-rolons ark-value) [uuid])
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn get-changes-by-property
  ([ark-value rolon-uuid property-path]
   (let [changes-by-property (get-changes-by-property ark-value rolon-uuid)
         pc (get changes-by-property property-path)]
     (if (nil? pc)
       nil
       (mapish/mi-sub pc nil nil <= (get-selected-time ark-value)))))
  ([ark-value rolon-uuid]
   (:changes-by-property (get-rolon ark-value rolon-uuid))))

(defn get-rolon-uuid
  "returns the uuid of the rolon"
  [rolon]
  (:rolon-uuid rolon))

(defn ark-str
  [ark-value]
  (let [s (str "\n" :ark "\n"
               "\n" :index-rolons "\n\n" (seq (get-indexes ark-value)) "\n"
               "\n" :journal-entry-rolons "\n\n" (seq (get-journal-entries ark-value)) "\n"
               "\n" :random-rolons "\n\n" (get-random-rolons ark-value))]
    s))

(defmethod print-method Ark-value
  [ark writer]
  (print-simple (ark-str ark) writer))

(defn assoc-rolon
  "update the ark with the revised/new rolon"
  [ark-value rolon-uuid rolon]
  (cond
    (uuid/journal-entry-uuid? rolon-uuid)
    (let [journal-entries (get-journal-entries ark-value)
          journal-entries (assoc journal-entries [rolon-uuid] rolon)]
      (assoc ark-value :journal-entries journal-entries))
    (uuid/index-uuid? rolon-uuid)
    (let [indexes (get-indexes ark-value)
          indexes (assoc indexes [rolon-uuid] rolon)]
      (assoc ark-value :indexes indexes))
    (uuid/random-uuid? rolon-uuid)
    (let [rolons (get-random-rolons ark-value)
          rolons (assoc rolons [rolon-uuid] rolon)]
      (assoc ark-value :random-rolons rolons))
    :else (throw (Exception. (str rolon-uuid " is unrecognized")))))

(defn update-changes-for-property
  [ark-value ark-db changes-by-property je-uuid property-name new-value]
  (let [changes-by-property (if (some? changes-by-property)
                              changes-by-property
                              (create-mi ark-db))]
    (assoc changes-by-property
      property-name
      (let [property-changes (get changes-by-property property-name)
            property-changes (if (some? property-changes)
                               property-changes
                               (create-mi ark-db))
            first-entry (first (seq property-changes))]
        (if (or (nil? first-entry) (not= new-value (val first-entry)))
          (assoc property-changes [je-uuid] new-value)
          property-changes)))))

(defn update-rolon-properties
  [ark-value ark-db rolon je-uuid properties]
  (let [rolon-uuid (get-rolon-uuid rolon)
        changes (get-changes-by-property ark-value rolon-uuid)
        changes (reduce
                  (fn [ch pe]
                    (update-changes-for-property ark-value ark-db ch je-uuid (key pe) (val pe)))
                  changes
                  (seq properties))]
    (assoc rolon :changes-by-property changes)))

(defn get-property-value
  [ark-value rolon-uuid property-path]
  (mapish/validate-property-path property-path)
  (let [changes (get-changes-by-property ark-value rolon-uuid property-path)]
    (if changes
      (val (first (rseq (mapish/mi-sub changes nil nil <= (get-selected-time ark-value)))))
      nil)))

(defn get-property-values
  ([ark-value rolon-uuid]
   (get-property-values ark-value rolon-uuid (get-changes-by-property ark-value rolon-uuid)))
  ([ark-value rolon-uuid all-changes]
   (miView/->MI-view ark-value rolon-uuid all-changes (get-selected-time ark-value))))

(defn index-lookup
  "returns the uuids for a given index-uuid and value"
  [ark-value index-uuid value]
  (map
    (fn [e]
      ((key e) 2))
    (filter
      #(some? (val %))
      (seq (mapish/mi-sub
             (get-property-values ark-value index-uuid)
             [:content/index value])))))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-value index-name]
  (first (index-lookup ark-value (index-name-uuid ark-value) index-name)))

(defn name-lookup
  [ark-value rolon-name]
  (let [name-index-uuid (get-index-uuid ark-value "name")]
    (index-lookup ark-value name-index-uuid rolon-name)))

(defn get-content-index
  "returns a seq of [value uuid]"
  [ark-value index-uuid]
  (map
    (fn [e]
      (let [v (key e)]
        [(v 1) (v 2)]))
    (filter
      #(some? (val %))
      (seq (mapish/mi-sub
             (get-property-values ark-value index-uuid)
             [:content/index])))))

(defn get-related-uuids
  "returns a lazy seq of the related uuids"
  [ark-value uuid relation-keyword]
  (map
    (fn [e]
      ((key e) 1))
    (filter
      #(val %)
      (seq (mapish/mi-sub (get-property-values ark-value uuid) [relation-keyword])))))

(declare update-properties- je-modified)

(defn update-properties
  [ark-value ark-db rolon-uuid properties]
  (mapish/validate-properties properties)
  (let [journal-entry-uuid (get-latest-journal-entry-uuid ark-value)
        ark-value (update-properties- ark-value ark-db journal-entry-uuid rolon-uuid properties)]
    (je-modified ark-value ark-db rolon-uuid)))

(defn update-property
  [ark-value ark-db rolon-uuid property-path property-value]
  (mapish/validate-property-path property-path)
  (update-properties ark-value ark-db rolon-uuid (create-mi ark-db property-path property-value)))

(defn make-rolon
  [ark-value ark-db rolon-uuid properties]
  (mapish/validate-properties properties)
  (let [ark-value
        (if (get-rolon ark-value rolon-uuid)
          ark-value
          (assoc-rolon
            ark-value
            rolon-uuid
            (rolonRecord/->Rolon-record rolon-uuid)))]
    (update-properties ark-value ark-db rolon-uuid properties)))

(defn make-index-rolon-
  [ark-value ark-db index-keyword value uuid adding]
  (let [iuuid (uuid/index-uuid (get-ark-db ark-value) index-keyword)
        ark-value (if (get-rolon ark-value iuuid)
                    ark-value
                    (make-rolon ark-value ark-db iuuid
                                (create-mi
                                  ark-db
                                  [:index/index.name] (name index-keyword))))]
    (update-property ark-value
                     ark-db
                     iuuid
                     [:content/index value uuid]
                     adding)))

(defn make-index-rolon
  "create/update an index rolon"
  [ark-value ark-db uuid properties old-properties]
  (reduce #(let [ark-value %1
                 path (key %2)
                 k (first path)
                 nv (val %2)
                 ov (get old-properties path)
                 ark-value (if (and ov (mapish/index? k))
                             (make-index-rolon- ark-value ark-db k ov uuid nil)
                             ark-value)
                 ark-value (if (and nv (mapish/index? k))
                             (make-index-rolon- ark-value ark-db k nv uuid true)
                             ark-value)]
            ark-value)
          ark-value (seq properties)))

(defn update-properties-
  [ark-value ark-db journal-entry-uuid rolon-uuid properties]
  (let [property-values (get-property-values ark-value rolon-uuid)
        ark-value (make-index-rolon ark-value
                                    ark-db
                                    rolon-uuid
                                    properties
                                    property-values)
        rolon (get-rolon ark-value rolon-uuid)
        rolon (update-rolon-properties ark-value ark-db rolon journal-entry-uuid properties)]
    (assoc-rolon ark-value rolon-uuid rolon)))

(defn update-property-
  [ark-value ark-db journal-entry-uuid rolon-uuid property-path property-value]
  (update-properties- ark-value
                      ark-db
                      journal-entry-uuid
                      rolon-uuid
                      (create-mi ark-db property-path property-value)))

(defn update-relation
  [ark-value ark-db relaton-name from-uuid to-uuid symmetrical add]
  (let [[rel irel] (if symmetrical
                     [(keyword "bi-rel" relaton-name) (keyword "bi-rel" relaton-name)]
                     [(keyword "rel" relaton-name) (keyword "inv-rel" relaton-name)])
        add (if add true nil)
        journal-entry-uuid (get-latest-journal-entry-uuid ark-value)
        ark-value (update-property- ark-value
                                    ark-db
                                    journal-entry-uuid
                                    from-uuid
                                    [rel to-uuid]
                                    add)]
    (update-property- ark-value
                      ark-db
                      journal-entry-uuid
                      to-uuid
                      [irel from-uuid]
                      add)))

(defn je-modified
  "track the rolons modified by the journal entry"
  [ark-value ark-db rolon-uuid]
  (let [je-uuid (get-latest-journal-entry-uuid ark-value)]
    (update-relation ark-value ark-db "modified" je-uuid rolon-uuid false true)))

(defn destroy-rolon
  [ark-value ark-db rolon-uuid]
  (let [old-property-values (get-property-values ark-value rolon-uuid)
        property-values (reduce #(assoc %1 (key %2) nil)
                                (create-mi ark-db)
                                (seq old-property-values))
        ark-value (make-index-rolon ark-value
                                    ark-db
                                    rolon-uuid
                                    property-values
                                    old-property-values)
        je-uuid (get-latest-journal-entry-uuid ark-value)
        rolon (get-rolon ark-value rolon-uuid)
        rolon (update-rolon-properties ark-value ark-db rolon je-uuid property-values)
        ark-value (assoc-rolon ark-value rolon-uuid rolon)]
    (je-modified ark-value ark-db rolon-uuid)))

(defmulti eval-transaction (fn [ark-value ark-db n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-value ark-db n s]
  (let [je-uuid (get-latest-journal-entry-uuid ark-value)
        [rolon-uuid je-properties-map rolon-properties-map] (read-string s)
        je-properties (create-mi ark-db [:index/headline] (str "update a rolon with " s))
        je-properties (into je-properties je-properties-map)
        rolon-properties (into (create-mi ark-db) rolon-properties-map)]
    (-> ark-value
        (update-properties ark-db je-uuid je-properties)
        (make-rolon ark-db rolon-uuid rolon-properties))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-value ark-db n s]
  (let [je-uuid (get-latest-journal-entry-uuid ark-value)
        [uuid je-properties-map] (read-string s)
        je-properties (create-mi ark-db [:index/headline] (str "destroy rolon " s))
        je-properties (into je-properties je-properties-map)]
    (-> ark-value
        (update-properties ark-db je-uuid je-properties)
        (destroy-rolon ark-db uuid))))

(defn reprocess-trans
  [m seq]
  (reduce (fn [_ [je-uuid transaction-name s _]]
            (ark-db/process-transaction! m je-uuid transaction-name s))
          nil
          seq))
