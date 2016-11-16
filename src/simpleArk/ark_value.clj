(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.mapish :as mapish]
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

(defn assoc-rolon
  "update the ark with the revised/new rolon"
  [ark-record rolon-uuid rolon-record]
  (cond
    (uuid/journal-entry-uuid? rolon-uuid)
    (let [journal-entries (arkRecord/get-journal-entries ark-record)
          journal-entries (assoc journal-entries [rolon-uuid] rolon-record)]
      (assoc ark-record :journal-entries journal-entries))
    (uuid/index-uuid? rolon-uuid)
    (let [indexes (arkRecord/get-indexes ark-record)
          indexes (assoc indexes [rolon-uuid] rolon-record)]
      (assoc ark-record :indexes indexes))
    (uuid/random-uuid? rolon-uuid)
    (let [rolons (arkRecord/get-random-rolons ark-record)
          rolons (assoc rolons [rolon-uuid] rolon-record)]
      (assoc ark-record :random-rolons rolons))
    :else (throw (Exception. (str rolon-uuid " is unrecognized")))))

(defn update-changes-for-property
  [changes-by-property component-map je-uuid property-name new-value]
  (let [changes-by-property (if (some? changes-by-property)
                              changes-by-property
                              (create-mi component-map))]
    (assoc changes-by-property
      property-name
      (let [property-changes (get changes-by-property property-name)
            property-changes (if (some? property-changes)
                               property-changes
                               (create-mi component-map))
            first-entry (first (seq property-changes))]
        (if (or (nil? first-entry) (not= new-value (val first-entry)))
          (assoc property-changes [je-uuid] new-value)
          property-changes)))))

(defn update-rolon-properties
  [rolon-record ark-record component-map je-uuid properties]
  (let [rolon-uuid (rolonRecord/get-rolon-uuid rolon-record)
        changes (arkRecord/get-changes-by-property ark-record rolon-uuid)
        changes (reduce
                  (fn [ch pe]
                    (update-changes-for-property ch component-map je-uuid (key pe) (val pe)))
                  changes
                  (seq properties))]
    (assoc rolon-record :changes-by-property changes)))

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
  (let [property-values (arkRecord/get-property-values ark-value rolon-uuid)
        ark-value (make-index-rolon ark-value
                                    ark-db
                                    rolon-uuid
                                    properties
                                    property-values)
        rolon (arkRecord/get-rolon ark-value rolon-uuid)
        rolon (update-rolon-properties rolon ark-value ark-db journal-entry-uuid properties)]
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
  (let [old-property-values (arkRecord/get-property-values ark-value rolon-uuid)
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
        rolon (update-rolon-properties rolon ark-value ark-db je-uuid property-values)
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
