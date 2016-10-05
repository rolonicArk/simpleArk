(ns simpleArk.ark-value
  (:require [simpleArk.uuid :as uuid]
            [simpleArk.ark-db :as ark-db]
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
                make-rolon! destroy-rolon! update-properties! update-ark!
                get-current-journal-entry-uuid
                select-time get-selected-time index-name-uuid])

(defrecord Rolon [rolon-uuid get-rolon-values])

(defrecord Rolon-value [journal-entry-uuid rolon-uuid
                        get-property-values get-property-journal-entry-uuids])

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

(defn validate-property-keys
  "properties must be classifiers or descriptors"
  [properties]
  (reduce #(if (not (or (classifier? %2) (descriptor? %2)))
            (throw (Exception. (str %2 " is neither a classifier nor a keyword"))))
          nil (keys properties)))

(defn make-rolon!
  [ark-value rolon-uuid properties]
  (validate-property-keys properties)
  ((:make-rolon! ark-value) ark-value rolon-uuid properties))

(defn destroy-rolon!
  "deletes all the classifiers of a rolon"
  [ark-value rolon-uuid]
  ((:destroy-rolon! ark-value) ark-value rolon-uuid))

(defn update-properties!
  "update the properties of a rolon"
  [ark-value rolon-uuid properties]
  (validate-property-keys properties)
  ((:update-properties! ark-value) ark-value rolon-uuid properties))

(defn update-property!
  "update the value of a property of a rolon"
  [ark-value rolon-uuid property-name property-value]
  (update-properties! ark-value rolon-uuid (sorted-map property-name property-value)))

(defn get-rolon-uuid
  "returns the uuid of the rolon,
  where rec is a rolon or rolon-value"
  [rec]
  (:rolon-uuid rec))

(defn get-rolon-values
  "returns a sorted map of all the values of a rolon"
  [rolon]
  ((:get-rolon-values rolon) rolon))

(defmethod print-method Rolon
  [rolon writer]
  (print-simple
    (str "\n" :rolon "\n"
         "\n" (mapish/mi-seq (get-rolon-values rolon)) "\n\n")
    writer))

(defn get-journal-entry-uuid
  "returns the type-1 uuid of the journal entry rolon which created this rolon value"
  [rolon-value]
  (:journal-entry-uuid rolon-value) rolon-value)

(defn get-property-values
  "returns the values of the properties, nil indicating the property is no longer present"
  [rolon-value]
  ((:get-property-values rolon-value) rolon-value))

(defn get-property-je-uuids
  "returns the type 1 uuid of the journal entry rolons which changed each property"
  [rolon-value]
  ((:get-property-journal-entry-uuids rolon-value) rolon-value))

(defmethod print-method Rolon-value
  [rolon-value writer]
  (print-simple
    (str "\n" :properties "\n" (get-property-values rolon-value)
         "\n" :journal-entry-uuids "\n" (get-property-je-uuids rolon-value) "\n\n")
    writer))

(defn get-rolon-value-at
  "returns the rolon value for the selected time"
  [ark-value rolon-uuid je-uuid]
  (let [rolon (get-rolon ark-value rolon-uuid)]
    (val (first (mapish/mi-rseq (mapish/mi-sub (get-rolon-values rolon) nil nil <= je-uuid))))))

(defn get-current-rolon-value
  "returns the rolon value for the selected time"
  [ark-value rolon-uuid]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)]
    (get-rolon-value-at ark-value rolon-uuid je-uuid)))

(defn get-property-values-at
  "returns the property values at the selected time"
  [ark-value rolon-uuid je-uuid]
  (get-property-values (get-rolon-value-at ark-value rolon-uuid je-uuid)))

(defn get-current-property-values
  "returns the property values at the selected time"
  [ark-value rolon-uuid]
  (get-property-values (get-current-rolon-value ark-value rolon-uuid)))

(defn get-property-je-uuids-at
  "returns the journal entries which last changed each property
  at the selected time"
  [ark-value rolon-uuid je-uuid]
  (get-property-je-uuids (get-rolon-value-at ark-value rolon-uuid je-uuid)))

(defn get-current-property-je-uuids
  "returns the journal entries which last changed each property
  at the selected time"
  [ark-value rolon-uuid]
  (get-property-je-uuids (get-current-rolon-value ark-value rolon-uuid)))

(defn get-property-value-at
  "returns the value of a property"
  [ark-value rolon-uuid key je-uuid]
  ((get-property-values-at ark-value rolon-uuid je-uuid) key))

(defn get-current-property-value
  "returns the current value of a property"
  [ark-value rolon-uuid key]
  ((get-current-property-values ark-value rolon-uuid) key))

(defn get-property-je-uuid-at
  "returns the uuid of the last je that changed the property
  at the selected time"
  [ark-value rolon-uuid key je-uuid]
  (key (get-property-je-uuids-at ark-value rolon-uuid je-uuid)))

(defn get-current-property-je-uuid
  "returns the uuid of the last je that changed the property
  at the selected time"
  [ark-value rolon-uuid key]
  (key (get-current-property-je-uuids ark-value rolon-uuid)))

(defn get-previous-rolon-je-uuid
  "returns the uuid of the prior journal entry updating the same rolon"
  [ark-value rolon-uuid je-uuid]
  (let [rolon (get-rolon ark-value rolon-uuid)
        rolon-values (get-rolon-values rolon)
        previous-rolon-values (mapish/mi-rseq (mapish/mi-sub rolon-values nil nil < je-uuid))]
    (key (first previous-rolon-values))))

(defn locate-next-je-uuid-for-property
  ([[ark-value rolon-uuid key je-uuid]]
   (let [je-uuid2 (get-property-je-uuid-at ark-value rolon-uuid key je-uuid)]
     (if (= je-uuid je-uuid2)
       (let [rolon-value (get-previous-rolon-je-uuid ark-value rolon-uuid je-uuid)]
         (if rolon-value
           (let [je-uuid2 (get-journal-entry-uuid rolon-value)
                 je-uuid2 (get-property-je-uuid-at ark-value rolon-uuid key je-uuid2)]
             (if je-uuid2
               [ark-value rolon-uuid key je-uuid2]
               nil))
           nil))
       (if je-uuid2
         [ark-value rolon-uuid key je-uuid2]
         nil)))))

(defn rolon-property-je-uuids-at
  "returns a lazy sequence of journal entry uuids which changed a property"
  [ark-value rolon-uuid key je-uuid]
  (map #(if %
         (% 3)
         nil)
       (take-while identity (iterate locate-next-je-uuid-for-property [ark-value rolon-uuid key je-uuid]))))

(defn rolon-property-current-je-uuids
  "returns a lazy sequence of journal entry uuids which changed a property"
  [ark-value rolon-uuid key]
  (let [first-je-uuid (get-current-property-je-uuid ark-value rolon-uuid key)]
    (if first-je-uuid
      (rolon-property-je-uuids-at ark-value rolon-uuid key first-je-uuid)
      nil)))

(defn index-lookup
  "returns the uuids for a given index-uuid and value and time"
  [ark-value index-uuid value]
  (let [properties (get-current-property-values ark-value index-uuid)
        index-map (:descriptor/index properties)]
    (index-map value)))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [ark-value index-name]
  (first (index-lookup ark-value (index-name-uuid ark-value) index-name)))

(defn name-lookup
  [ark-value rolon-name]
  (let [name-index-uuid (get-index-uuid ark-value "name")]
    (first (index-lookup ark-value name-index-uuid rolon-name))))

(defn get-updated-rolon-uuids
  "returns a map of the uuids of the rolons updated by a journal-entry rolon"
  [ark-value je-uuid]
  (let [latest-je-property-values (get-current-property-values ark-value je-uuid)
        updated-rolon-uuids (:descriptor/updated-rolon-uuids latest-je-property-values)]
    (if (nil? updated-rolon-uuids)
      (sorted-map)
      updated-rolon-uuids)))

(defn get-index-descriptor
  "returns a sorted map of sets of rolon uuids keyed by classifier value"
  [ark-value je-uuid]
  (let [index (:descriptor/index (get-current-property-values ark-value je-uuid))]
    (if (nil? index)
      (sorted-map)
      index)))

(defn make-index-rolon-!
  [ark-value classifier value uuid adding]
  (let [iuuid (uuid/index-uuid (get-ark-db ark-value) classifier)
        properties (if (get-rolon ark-value iuuid)
                     (sorted-map)
                     (sorted-map :classifier/index.name (name classifier)))
        ark-value (make-rolon! ark-value iuuid properties)
        index-rolon (get-rolon ark-value iuuid)
        index-descriptor (get-index-descriptor ark-value iuuid)
        value-set (index-descriptor value)
        value-set (if value-set value-set #{})
        value-set (if adding
                    (conj value-set uuid)
                    (disj value-set uuid))
        index-descriptor (assoc index-descriptor value value-set)]
    (update-property! ark-value (get-rolon-uuid index-rolon) :descriptor/index index-descriptor)))

(defn make-index-rolon!
  "create/update an index rolon"
  [ark-value uuid properties old-properties]
  (reduce #(let [ark-value %1
                 k (key %2)
                 nv (val %2)
                 ov (old-properties k)
                 ark-value (if (and ov (classifier? k))
                             (make-index-rolon-! ark-value k ov uuid false)
                             ark-value)
                 ark-value (if (and nv (classifier? k))
                             (make-index-rolon-! ark-value k nv uuid true)
                             ark-value)]
            ark-value)
          ark-value properties))

(defmulti eval-transaction (fn [ark-value n s] n))

(defmethod eval-transaction :ark/update-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)
        [rolon-uuid je-properties rolon-properties] (read-string s)
        je-properties (into {:classifier/headline (str "update a rolon with " s)} je-properties)]
    (-> ark-value
        (update-properties! je-uuid je-properties)
        (make-rolon! rolon-uuid rolon-properties))))

(defmethod eval-transaction :ark/destroy-rolon-transaction!
  [ark-value n s]
  (let [je-uuid (get-current-journal-entry-uuid ark-value)
        [uuid je-properties] (read-string s)
        je-properties (into {:classifier/headline (str "destroy rolon " s)} je-properties)]
    (-> ark-value
        (update-properties! je-uuid je-properties)
        (destroy-rolon! uuid))))

(defn reprocess-trans
  [m seq]
  (reduce (fn [_ [je-uuid transaction-name s _]]
            (ark-db/process-transaction! m je-uuid transaction-name s))
          nil
          seq))
