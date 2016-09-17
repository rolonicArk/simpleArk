(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.uuid :as uuid]))

(set! *warn-on-reflection* true)

(defn get-current-journal-entry-uuid
  [ark-value]
  (::active-journal-entry-uuid ark-value))

(defn get-journal-entries
  [ark-value]
  (::journal-entries ark-value))

(defn get-indexes
  [ark-value]
  (::indexes ark-value))

(defn get-random-rolons
  [ark-value]
  (::random-rolons ark-value))

(defn get-rolon
  [ark-value uuid]
  (cond
    (uuid/journal-entry-uuid? uuid) ((ark-value/get-journal-entries ark-value) uuid)
    (uuid/index-uuid? uuid) ((ark-value/get-indexes ark-value) uuid)
    (uuid/random-uuid? uuid) ((ark-value/get-random-rolons ark-value) uuid)
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn assoc-rolon!
  "update the ark with the revised/new rolon"
  ([rolon-uuid rolon]
   (vswap! ark-value/*volatile-ark-value* assoc-rolon! rolon-uuid rolon))
  ([ark-value rolon-uuid rolon]
   (cond
     (uuid/journal-entry-uuid? rolon-uuid)
     (assoc-in ark-value [::journal-entries rolon-uuid] rolon)
     (uuid/index-uuid? rolon-uuid)
     (assoc-in ark-value [::indexes rolon-uuid] rolon)
     (uuid/random-uuid? rolon-uuid)
     (assoc-in ark-value [::random-rolons rolon-uuid] rolon)
     :else (throw (Exception. (str rolon-uuid " is unrecognized"))))))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn update-properties-!
  ([journal-entry-uuid rolon-uuid properties]
   (vswap! ark-value/*volatile-ark-value* update-properties-! journal-entry-uuid rolon-uuid properties))
  ([ark-value journal-entry-uuid rolon-uuid properties]
   (let [rolon (ark-value/get-rolon ark-value rolon-uuid)
         rolon-value (ark-value/get-current-rolon-value ark-value rolon-uuid)
         property-values (::property-values rolon-value)
         ark-value (ark-value/make-index-rolon! rolon-uuid properties property-values) ;todo
         property-values (into property-values properties)
         rolon-value (assoc rolon-value ::property-values property-values)
         pjes (::property-journal-entry-uuids rolon-value)
         pjes (update-property-journal-entry-uuids pjes properties journal-entry-uuid)
         rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
         rolon (assoc-in rolon [::rolon-values journal-entry-uuid] rolon-value)]
     (assoc-rolon! ark-value rolon-uuid rolon))))

(defn update-property-!
  ([journal-entry-uuid rolon-uuid property-name property-value]
   (vswap! ark-value/*volatile-ark-value* update-property-! journal-entry-uuid rolon-uuid property-name property-value))
  ([ark-value journal-entry-uuid rolon-uuid property-name property-value]
   (update-properties-! ark-value journal-entry-uuid rolon-uuid (sorted-map property-name property-value))))

(defn je-modified!
  "track the rolons modified by the journal entry"
  ([journal-entry-uuid rolon-uuid]
   (vswap! ark-value/*volatile-ark-value* je-modified! journal-entry-uuid rolon-uuid))
  ([ark-value journal-entry-uuid rolon-uuid]
   (let [je-value (ark-value/get-current-rolon-value ark-value journal-entry-uuid)
         je-property-values (::property-values je-value)
         modified (:descriptor/modified je-property-values)
         modified (if modified
                    (conj modified rolon-uuid)
                    (sorted-set rolon-uuid))]
     (update-property-! ark-value journal-entry-uuid journal-entry-uuid :descriptor/modified modified))))

(defn destroy-rolon!
  ([rolon-uuid]
   (vswap! ark-value/*volatile-ark-value* destroy-rolon! rolon-uuid))
  ([ark-value rolon-uuid]
   (let [je-uuid (::active-journal-entry-uuid ark-value)
         rolon (ark-value/get-rolon ark-value rolon-uuid)
         rolon-value (ark-value/get-current-rolon-value ark-value rolon-uuid)
         old-property-values (::property-values rolon-value)
         property-values (reduce #(assoc %1 %2 nil) (sorted-map) (keys old-property-values))
         ark-value (ark-value/make-index-rolon! rolon-uuid property-values old-property-values)
         rolon-value (assoc rolon-value ::property-values property-values)
         pjes (::property-journal-entry-uuids rolon-value)
         pjes (reduce #(assoc %1 %2 je-uuid) (sorted-map) (keys pjes))
         rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
         rolon (assoc-in rolon [::rolon-values je-uuid] rolon-value)
         ark-value (assoc-rolon! ark-value rolon-uuid rolon)]
     (je-modified! ark-value je-uuid rolon-uuid))))

(defn update-properties!
  [rolon-uuid properties]
  (let [journal-entry-uuid (::active-journal-entry-uuid @ark-value/*volatile-ark-value*)]
    (update-properties-! journal-entry-uuid rolon-uuid properties)
    (je-modified! journal-entry-uuid rolon-uuid)))

#_(defn update-properties!
  ([rolon-uuid properties]
   (vswap! ark-value/*volatile-ark-value* update-properties! rolon-uuid properties))
  ([ark-value rolon-uuid properties]
   (let [journal-entry-uuid (::active-journal-entry-uuid ark-value)
         ark-value (update-properties-! ark-value journal-entry-uuid rolon-uuid properties)]
     (je-modified! ark-value journal-entry-uuid rolon-uuid))))

(defn get-property-values
  [rolon-value]
  (::property-values rolon-value))

(defn get-property-journal-entry-uuids
  [rolon-value]
  (::property-journal-entry-uuids rolon-value))

(defn create-rolon-value
  "returns a new rolon value"
  [je-uuid rolon-uuid ps]
  (let [rolon-value (ark-value/->Rolon-value je-uuid rolon-uuid
                                             get-property-values get-property-journal-entry-uuids)
        rolon-value (assoc rolon-value ::property-values ps)
        rolon-value (assoc rolon-value ::property-journal-entry-uuids
                      (update-property-journal-entry-uuids (sorted-map) ps je-uuid))]
    rolon-value))

(defn get-rolon-values
  [rolon]
  (::rolon-values rolon))

(defn make-rolon!
  [rolon-uuid properties]
  (if (ark-value/get-rolon rolon-uuid)
    (update-properties! rolon-uuid properties)
    (let [je-uuid (::active-journal-entry-uuid @ark-value/*volatile-ark-value*)
          rolon (ark-value/->Rolon rolon-uuid get-rolon-values)
          rolon (assoc rolon ::rolon-values
                             (sorted-map je-uuid
                                         (create-rolon-value je-uuid
                                                             rolon-uuid
                                                             properties)))]
      (assoc-rolon! rolon-uuid rolon)
      (je-modified! je-uuid rolon-uuid)
      (ark-value/make-index-rolon! rolon-uuid properties (sorted-map)))))

(defn select-time!
  [ark-value je-uuid]
  (let [je-uuid (key (first (rsubseq (ark-value/get-journal-entries) <= je-uuid)))]
    (-> ark-value
        (assoc ::selected-time je-uuid)
        (assoc ::active-journal-entry-uuid je-uuid))))

(defn get-selected-time
  [ark-value]
  (::selected-time ark-value))

(defn index-name-uuid
  [ark-value]
  (::index-name-uuid ark-value))

(defn update-ark!
  [ark-value je-uuid transaction-name s]
  (let [ark-value (-> ark-value
                (assoc ::latest-journal-entry-uuid je-uuid)
                (assoc ::active-journal-entry-uuid je-uuid)
                (ark-value/ark-binder (fn []
                                        (vreset! ark-value/*volatile-ark-value*
                                                 (ark-value/make-rolon! je-uuid
                                                                        {:classifier/transaction-name transaction-name
                                                                         :descriptor/transaction-argument s}))
                                        (ark-value/eval-transaction transaction-name s))))]
    (if (::selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn create-ark
  [this-db]
  (-> (ark-value/->Ark this-db get-rolon get-journal-entries get-indexes get-random-rolons
                       make-rolon! destroy-rolon! update-properties! update-ark!
                       get-current-journal-entry-uuid
                       select-time! get-selected-time index-name-uuid)
      (assoc ::journal-entries (sorted-map))
      (assoc ::indexes (sorted-map))
      (assoc ::random-rolons {})
      (assoc ::index-name-uuid (uuid/index-uuid this-db :classifier/index.name))))

(defn- build
  "returns an ark db"
  [m]
  (assoc m :ark-value/create-ark create-ark))

(defn builder
  []
  build)
