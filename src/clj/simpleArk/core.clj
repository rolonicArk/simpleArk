(ns simpleArk.core
  (:require [clj-uuid :as uuid]))

(defn classifier?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "classifier" (namespace kw)))))

(defn descriptor?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "descriptor" (namespace kw)))))

(defn journal-entry-uuid
  []
  (uuid/v1))

(defn random-uuid
  []
  (uuid/v4))

(defn index-uuid
  [classifier]
  (if (not (classifier? classifier))
    (throw (Exception. (str classifier " is not a classifier keyword"))))
  (uuid/v5 uuid/+null+ (name classifier)))

(defn journal-entry-uuid?
  [uuid]
  (and (uuid? uuid)
       (= (uuid/get-version uuid) 1)))

(defn random-uuid?
  [uuid]
  (and (uuid? uuid)
       (= (uuid/get-version uuid) 4)))

(defn index-uuid?
  [uuid]
  (and (uuid? uuid)
       (= (uuid/get-version uuid) 5)))

;;a well known uuid
(def index-name-uuid (index-uuid :classifier/index.name))

(defprotocol Ark-db
  (get-ark [this]
    "returns the current value of the ark")
  (register-transaction! [this transaction-name f]
    "defines a transaction,
    where f takes an (edn) string")
  (process-transaction! [this transaction-name s]
    "process a transaction with an (edn) string,
    returning the new journal-entry uuid")
  (process-transaction-at! [this je-uuid transaction-name s]
    "process a transaction at a given time with an (edn) string"))


(def ^:dynamic *ark* nil)

(defn ark-binder
  [ark f]
  (let [vark (volatile! nil)]
    (binding [*ark* (volatile! ark)]
      (f)
      (vreset! vark @*ark*))
    @vark))

(defmacro bind-ark
  "binds a volatile holding an ark value to *ark* while body is evaluated,
  returning the last bound value of ark"
  [ark-db & body]
  `(ark-binder (get-ark ~ark-db) (fn [] ~@body)))

(defn get-current-journal-entry-uuid
  [ark]
  ((:get-current-journal-entry-uuid ark) ark))

(defrecord Ark [get-rolon get-journal-entries get-indexes get-random-rolons
                make-rolon destroy-rolon update-properties
                get-current-journal-entry-uuid
                select-time get-selected-time])

(defrecord Rolon [rolon-uuid get-rolon-values])

(defrecord Rolon-value [journal-entry-uuid rolon-uuid
                        get-property-values get-property-journal-entry-uuids])

(defn select-time
  "Sets the ark to the time of the journal entry uuid"
  [je-uuid]
  (vswap! *ark* #((:select-time %) % je-uuid)))

(defn get-selected-time
  "returns the journal entry uuid of the selected time"
  []
  ((:get-selected-time @*ark*) @*ark*))

(defn get-rolon
  "returns the rolon identified by the uuid, or nil"
  [ark uuid]
  ((:get-rolon ark) ark uuid))

(defn get-journal-entries
  "returns a sorted map of all the journal entry rolons"
  [ark]
  ((:get-journal-entries ark) ark))

(defn get-indexes
  "returns a sorted map of all the index rolons"
  [ark]
  ((:get-indexes ark) ark))

(defn get-random-rolons
  "returns a map of all the random rolons"
  [ark]
  ((:get-random-rolons ark) ark))

(defmethod print-method Ark
  [ark writer]
  (print-simple
    (str "\n" :ark "\n"
         "\n" :index-rolons "\n\n" (get-indexes ark) "\n"
         "\n" :journal-entry-rolons "\n\n" (get-journal-entries ark) "\n"
         "\n" :random-rolons "\n\n" (get-random-rolons ark))
    writer))

(defn validate-property-keys
  "properties must be classifiers or descriptors"
  [properties]
  (reduce #(if (not (or (classifier? %2) (descriptor? %2)))
            (throw (Exception. (str %2 " is neither a classifier nor a keyword"))))
          nil (keys properties)))

(defn make-rolon
  "returns a revised ark with the new rolon or updated"
  [ark rolon-uuid properties]
  (validate-property-keys properties)
  ((:make-rolon ark) ark rolon-uuid properties))

(defn destroy-rolon
  "deletes all the classifiers of a rolon"
  [rolon-uuid]
  (vreset! *ark* ((:destroy-rolon @*ark*) @*ark* rolon-uuid)))

(defn update-properties
  "update the properties of a rolon,
  returning an updated ark"
  [ark rolon-uuid properties]
  (validate-property-keys properties)
  ((:update-properties ark) ark rolon-uuid properties))

(defn update-property
  "update the value of a property of a rolon,
  returning an updated ark"
  [ark rolon-uuid property-name property-value]
  (update-properties ark rolon-uuid (sorted-map property-name property-value)))

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
         "\n" (get-rolon-values rolon) "\n\n")
    writer))

(defn get-journal-entry-uuid
  "returns the type-1 uuid of the journal entry rolon which created this rolon value"
  [rolon-value]
  (:journal-entry-uuid rolon-value) rolon-value)

(defn get-property-values
  "returns the values of the properties, nil indicating the property is no longer present"
  [rolon-value]
  ((:get-property-values rolon-value) rolon-value))

(defn get-property-journal-entry-uuids
  "returns the type 1 uuid of the journal entry rolons which changed each property"
  [rolon-value]
  ((:get-property-journal-entry-uuids rolon-value) rolon-value))

(defmethod print-method Rolon-value
  [rolon-value writer]
  (print-simple
    (str "\n" :properties "\n" (get-property-values rolon-value)
         "\n" :journal-entry-uuids "\n" (get-property-journal-entry-uuids rolon-value) "\n\n")
    writer))

(defn get-current-rolon-value
  "returns the current rolon value"
  [ark rolon-uuid]
  (let [je-uuid (get-current-journal-entry-uuid ark)
        rolon (get-rolon ark rolon-uuid)]
    (val (first (rsubseq (get-rolon-values rolon) <= je-uuid)))))

(defn get-current-property-values
  "returns the current property values"
  ([ark rolon-uuid]
   (get-property-values (get-current-rolon-value ark rolon-uuid))))

(defn index-lookup
  "returns the uuids for a given index-uuid and value and time"
  [index-uuid value]
  (let [properties (get-current-property-values @*ark* index-uuid)
        index-map (:descriptor/index properties)]
    (index-map value)))

(defn get-index-uuid
  "Looks up the index name in the index-name index rolon."
  [index-name]
  (first (index-lookup index-name-uuid index-name)))

(defn name-lookup
  [rolon-name]
  (let [name-index-uuid (get-index-uuid "name")]
    (first (index-lookup name-index-uuid rolon-name))))

(defn get-updated-rolon-uuids
  "returns a map of the uuids of the rolons updated by a journal-entry rolon"
  [je-uuid]
  (let [latest-je-property-values (get-current-property-values @*ark* je-uuid)
        updated-rolon-uuids (:descriptor/updated-rolon-uuids latest-je-property-values)]
    (if (nil? updated-rolon-uuids)
      (sorted-map)
      updated-rolon-uuids)))

(defn get-previous-value
  "returns the previous rolon value for the same rolon, or nil"
  [rolon-value]
  (let [journal-entry-uuid (get-journal-entry-uuid rolon-value)
        rolon (get-rolon @*ark* (get-rolon-uuid rolon-value))
        rolon-values (get-rolon-values rolon)
        previous-rolon-values (rsubseq rolon-values < journal-entry-uuid)]
    (val (first previous-rolon-values))))

(defn get-index-descriptor
  "returns a sorted map of sets of rolon uuids keyed by classifier value"
  [ark je-uuid]
  (let [index (:descriptor/index (get-current-property-values ark je-uuid))]
    (if (nil? index)
      (sorted-map)
      index)))

(defn make-index-rolon
  "create/update an index rolon, returning the updated ark"
  ([ark classifier value uuid adding]
   (let [iuuid (index-uuid classifier)
         properties (if (get-rolon ark iuuid)
                      (sorted-map)
                      (sorted-map :classifier/index.name (name classifier)))
         ark (make-rolon ark iuuid properties)
         index-rolon (get-rolon ark iuuid)
         index-descriptor (get-index-descriptor ark iuuid)
         value-set (index-descriptor value)
         value-set (if value-set value-set #{})
         value-set (if adding
                     (conj value-set uuid)
                     (disj value-set uuid))
         index-descriptor (assoc index-descriptor value value-set)]
     (update-property ark (get-rolon-uuid index-rolon) :descriptor/index index-descriptor)))
  ([ark uuid properties old-properties]
   (reduce #(let [ark %1
                  k (key %2)
                  nv (val %2)
                  ov (old-properties k)
                  ark (if (classifier? k)
                        (let [ark (if ov
                                    (make-index-rolon ark k ov uuid false)
                                    ark)
                              ark (if nv
                                    (make-index-rolon ark k nv uuid true)
                                    ark)]
                          ark)
                        ark)]
             ark)
           ark properties)))

(defn make-rolon-transaction
  [s]
  (let [je-uuid (get-current-journal-entry-uuid @*ark*)
        [rolon-uuid je-properties rolon-properties] (read-string s)
        je-properties (into {:classifier/headline (str "make a rolon with " s)} je-properties)
        ark (update-properties @*ark* je-uuid je-properties)
        ark (make-rolon ark rolon-uuid rolon-properties)]
    (vreset! *ark*  ark)))

(defn destroy-rolon-transaction
  [s]
  (let [je-uuid (get-current-journal-entry-uuid @*ark*)
        [uuid je-properties] (read-string s)
        je-properties (into {:classifier/headline (str "destroy rolon " s)} je-properties)]
    (vreset! *ark*  (update-properties @*ark* je-uuid je-properties))
    (destroy-rolon uuid)))
