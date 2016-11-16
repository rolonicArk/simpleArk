(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.mapish :as mapish]
            [simpleArk.miView :as miView]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.arkRecord :as arkRecord])
  (:import (java.util UUID)))

(set! *warn-on-reflection* true)

(defn create-mi
  [component-map & keyvals]
  (apply (:ark-value/create-mi component-map) component-map keyvals))

(defn ark-value-assoc-mapish
  [ark-value component-map key]
  (let [mi (create-mi component-map)]
    (assoc ark-value key mi)))

(defn init-ark-value
  [ark-value component-map]
  (-> ark-value
      (ark-value-assoc-mapish component-map :journal-entries)
      (ark-value-assoc-mapish component-map :indexes)
      (ark-value-assoc-mapish component-map :random-rolons)))

(defn create-ark
  [component-map]
  (-> (arkRecord/->Ark-record)
      (init-ark-value component-map)))

(defn get-rolon-uuid
  "returns the uuid of the rolon"
  [rolon]
  (:rolon-uuid rolon))

(defn assoc-rolon
  "update the ark with the revised/new rolon"
  [ark-value rolon-uuid rolon]
  (cond
    (uuid/journal-entry-uuid? rolon-uuid)
    (let [journal-entries (arkRecord/get-journal-entries ark-value)
          journal-entries (assoc journal-entries [rolon-uuid] rolon)]
      (assoc ark-value :journal-entries journal-entries))
    (uuid/index-uuid? rolon-uuid)
    (let [indexes (arkRecord/get-indexes ark-value)
          indexes (assoc indexes [rolon-uuid] rolon)]
      (assoc ark-value :indexes indexes))
    (uuid/random-uuid? rolon-uuid)
    (let [rolons (arkRecord/get-random-rolons ark-value)
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
        changes (arkRecord/get-changes-by-property ark-value rolon-uuid)
        changes (reduce
                  (fn [ch pe]
                    (update-changes-for-property ark-value ark-db ch je-uuid (key pe) (val pe)))
                  changes
                  (seq properties))]
    (assoc rolon :changes-by-property changes)))

(defn get-property-value
  [ark-value rolon-uuid property-path]
  (mapish/validate-property-path property-path)
  (let [changes (arkRecord/get-changes-by-property ark-value rolon-uuid property-path)]
    (if changes
      (val (first (rseq (mapish/mi-sub changes nil nil <= (arkRecord/get-selected-time ark-value)))))
      nil)))

(defn get-property-values
  ([ark-value rolon-uuid]
   (get-property-values ark-value rolon-uuid (arkRecord/get-changes-by-property ark-value rolon-uuid)))
  ([ark-value rolon-uuid all-changes]
   (miView/->MI-view ark-value rolon-uuid all-changes (arkRecord/get-selected-time ark-value))))

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

(def index-name-uuid (UUID/fromString "8cacc5db-70b3-5a83-85cf-c29541e14114"))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-value index-name]
  (first (index-lookup ark-value index-name-uuid index-name)))

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
  (let [journal-entry-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)
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
        (if (arkRecord/get-rolon ark-value rolon-uuid)
          ark-value
          (assoc-rolon
            ark-value
            rolon-uuid
            (rolonRecord/->Rolon-record rolon-uuid)))]
    (update-properties ark-value ark-db rolon-uuid properties)))

(defn make-index-rolon-
  [ark-value ark-db index-keyword value uuid adding]
  (let [iuuid (uuid/index-uuid ark-db index-keyword)
        ark-value (if (arkRecord/get-rolon ark-value iuuid)
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
        rolon (arkRecord/get-rolon ark-value rolon-uuid)
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
        journal-entry-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)
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
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)]
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
        je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)
        rolon (arkRecord/get-rolon ark-value rolon-uuid)
        rolon (update-rolon-properties ark-value ark-db rolon je-uuid property-values)
        ark-value (assoc-rolon ark-value rolon-uuid rolon)]
    (je-modified ark-value ark-db rolon-uuid)))

(defmulti eval-transaction (fn [ark-value ark-db n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-value ark-db n s]
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)
        [rolon-uuid je-properties-map rolon-properties-map] (read-string s)
        je-properties (create-mi ark-db [:index/headline] (str "update a rolon with " s))
        je-properties (into je-properties je-properties-map)
        rolon-properties (into (create-mi ark-db) rolon-properties-map)]
    (-> ark-value
        (update-properties ark-db je-uuid je-properties)
        (make-rolon ark-db rolon-uuid rolon-properties))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-value ark-db n s]
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)
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
