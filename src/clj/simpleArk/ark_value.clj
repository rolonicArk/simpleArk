(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.mapish :as mapish])
  (:import (clojure.lang Reversible Seqable ILookup IPersistentCollection)))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-value & keyvals] (apply (:create-mi ark-value) ark-value keyvals))

(defn ark-value-assoc-mapish
  [ark-value key]
  (let [mi (create-mi ark-value)]
    (assoc ark-value key mi)))

(defn init-ark-value
  [ark-value]
  (-> ark-value
      (ark-value-assoc-mapish :journal-entries)
      (ark-value-assoc-mapish :indexes)
      (ark-value-assoc-mapish :random-rolons)))

(defn create-ark
  [m]
  "returns a new ark"
  ((:ark-value/create-ark m) m))

(defrecord Ark-value [this-db make-rolon destroy-rolon update-properties update-ark create-mi])

(defrecord Rolon [rolon-uuid])

(defn index-name-uuid
  [ark-value]
  (uuid/index-uuid (:this-db ark-value) :classifier/index.name))

(defn get-selected-time
  "returns the journal entry uuid of the selected time"
  [ark-value]
  (:selected-time ark-value))

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
  [ark-value changes-by-property je-uuid property-name new-value]
  (let [changes-by-property (if (some? changes-by-property)
                              changes-by-property
                              (create-mi ark-value))]
    (assoc changes-by-property
      property-name
      (let [property-changes (get changes-by-property property-name)
            property-changes (if (some? property-changes)
                               property-changes
                               (create-mi ark-value))
            first-entry (first (seq property-changes))]
        (if (or (nil? first-entry) (not= new-value (val first-entry)))
          (assoc property-changes [je-uuid] new-value)
          property-changes)))))

(defn update-rolon-properties
  [ark-value rolon je-uuid properties]
  (let [rolon-uuid (get-rolon-uuid rolon)
        changes (get-changes-by-property ark-value rolon-uuid)
        changes (reduce
                  (fn [ch pe]
                    (update-changes-for-property ark-value ch je-uuid (key pe) (val pe)))
                  changes
                  (seq properties))]
    (assoc rolon :changes-by-property changes)))

(defn update-changes-by-property
  [ark-value changes-by-property je-uuid changed-properties]
  (reduce #(update-changes-for-property ark-value %1 je-uuid (key %2) (val %2))
          changes-by-property
          (seq changed-properties)))

(defn update-properties
  [ark-value rolon-uuid properties]
  (mapish/validate-property-paths properties)
  ((:update-properties ark-value) ark-value rolon-uuid properties))

(defn update-property
  [ark-value rolon-uuid property-path property-value]
  (mapish/validate-property-path property-path)
  (update-properties ark-value rolon-uuid (create-mi ark-value property-path property-value)))

(defn make-rolon
  [ark-value rolon-uuid properties]
  (mapish/validate-property-paths properties)
  ((:make-rolon ark-value) ark-value rolon-uuid properties))

(defn destroy-rolon
  "deletes all the classifiers of a rolon"
  [ark-value rolon-uuid]
  ((:destroy-rolon ark-value) ark-value rolon-uuid))

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
   (reify

     ILookup

     (valAt [this property-path default]
       (let [changes (get all-changes property-path)]
         (if (nil? changes)
           default
           (let [changes (mapish/mi-sub changes nil nil <= (get-selected-time ark-value))
                 fst (first (rseq changes))]
             (if (nil? fst)
               default
               (val fst))))))

     (valAt [this property-path]
       (get this property-path nil))

     Seqable

     (seq [this]
       (map
         #(clojure.lang.MapEntry. (key %) (val (val %)))
         (filter
           #(some? (val %))
           (map
             #(clojure.lang.MapEntry.
               (key %)
               (first (rseq (mapish/mi-sub (val %) nil nil <= (get-selected-time ark-value)))))
             (seq all-changes)))))

     IPersistentCollection

     (count [this]
       (count (seq this)))

     Reversible

     (rseq [this]
       (map
         #(clojure.lang.MapEntry. (key %) (val (val %)))
         (filter
           #(some? (val %))
           (map
             #(clojure.lang.MapEntry.
               (key %)
               (first (rseq (mapish/mi-sub (val %) nil nil <= (get-selected-time ark-value)))))
             (rseq all-changes)))))

     mapish/MI

     (mi-sub [this prefix]
       (get-property-values ark-value
                            rolon-uuid
                            (mapish/mi-sub all-changes prefix)))
     (mi-sub [this start-test start-key end-test end-key]
       (get-property-values ark-value
                            rolon-uuid
                            (mapish/mi-sub all-changes start-test start-key end-test end-key))))))

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
        [:descriptor/index value])))))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-value index-name]
  (first (index-lookup ark-value (index-name-uuid ark-value) index-name)))

(defn name-lookup
  [ark-value rolon-name]
  (let [name-index-uuid (get-index-uuid ark-value "name")]
    (index-lookup ark-value name-index-uuid rolon-name)))

(defn get-descriptor-index
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
                       [:descriptor/index])))))

(defn make-index-rolon-
  [ark-value classifier-keyword value uuid adding]
  (let [iuuid (uuid/index-uuid (get-ark-db ark-value) classifier-keyword)
        ark-value (if (get-rolon ark-value iuuid)
                    ark-value
                    (make-rolon ark-value iuuid
                                (create-mi
                                  ark-value
                                  [:classifier/index.name] (name classifier-keyword))))]
    (update-property ark-value
                     iuuid
                     [:descriptor/index value uuid]
                     adding)))

(defn make-index-rolon
  "create/update an index rolon"
  [ark-value uuid properties old-properties]
  (reduce #(let [ark-value %1
                 path (key %2)
                 k (first path)
                 nv (val %2)
                 ov (get old-properties path)
                 ark-value (if (and ov (mapish/classifier? k))
                             (make-index-rolon- ark-value k ov uuid nil)
                             ark-value)
                 ark-value (if (and nv (mapish/classifier? k))
                             (make-index-rolon- ark-value k nv uuid true)
                             ark-value)]
            ark-value)
          ark-value (seq properties)))

(defn get-updated-rolon-uuids
  "returns a lazy seq of the uuids of the rolons updated by a journal-entry rolon"
  [ark-value je-uuid]
  (map
    (fn [e]
      ((key e) 1))
    (seq (mapish/mi-sub (get-property-values ark-value je-uuid) [:descriptor/modified]))))

(defn get-modifying-journal-entry-uuids
  "returns a lazy seq of the uuids of the journal entries that updated a rolon"
  [ark-value rolon-uuid]
  (map
    (fn [e]
      ((key e) 1))
    (seq (mapish/mi-sub (get-property-values ark-value rolon-uuid) [:descriptor/journal-entry]))))

(defmulti eval-transaction (fn [ark-value n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-latest-journal-entry-uuid ark-value)
        [rolon-uuid je-properties-map rolon-properties-map] (read-string s)
        je-properties (create-mi ark-value [:classifier/headline] (str "update a rolon with " s))
        je-properties (into je-properties je-properties-map)
        rolon-properties (into (create-mi ark-value) rolon-properties-map)]
    (-> ark-value
        (update-properties je-uuid je-properties)
        (make-rolon rolon-uuid rolon-properties))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-latest-journal-entry-uuid ark-value)
        [uuid je-properties-map] (read-string s)
        je-properties (create-mi ark-value [:classifier/headline] (str "destroy rolon " s))
        je-properties (into je-properties je-properties-map)]
    (-> ark-value
        (update-properties je-uuid je-properties)
        (destroy-rolon uuid))))

(defn reprocess-trans
  [m seq]
  (reduce (fn [_ [je-uuid transaction-name s _]]
            (ark-db/process-transaction! m je-uuid transaction-name s))
          nil
          seq))
