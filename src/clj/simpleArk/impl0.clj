(ns simpleArk.impl0
  (:require [simpleArk.core :as ark]))

(defn get-current-journal-entry-uuid
  [ark]
  (::active-journal-entry-uuid ark))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn assoc-rolon!
  "update the ark with the revised/new rolon"
  [rolon-uuid rolon]
  (vreset! ark/*ark* (cond
                       (ark/journal-entry-uuid? rolon-uuid)
                       (assoc-in @ark/*ark* [::journal-entries rolon-uuid] rolon)
                       (ark/index-uuid? rolon-uuid)
                       (assoc-in @ark/*ark* [::indexes rolon-uuid] rolon)
                       (ark/random-uuid? rolon-uuid)
                       (assoc-in @ark/*ark* [::random-rolons rolon-uuid] rolon)
                       :else (throw (Exception. (str rolon-uuid " is unrecognized"))))))

(defn update-properties-!
  [journal-entry-uuid rolon-uuid properties]
  (let [rolon (ark/get-rolon @ark/*ark* rolon-uuid)
        rolon-value (ark/get-current-rolon-value @ark/*ark* rolon-uuid)
        property-values (::property-values rolon-value)
        _ (vreset! ark/*ark* (ark/make-index-rolon @ark/*ark* rolon-uuid properties property-values))
        property-values (into property-values properties)
        rolon-value (assoc rolon-value ::property-values property-values)
        pjes (::property-journal-entry-uuids rolon-value)
        pjes (update-property-journal-entry-uuids pjes properties journal-entry-uuid)
        rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
        rolon (assoc-in rolon [::rolon-values journal-entry-uuid] rolon-value)]
    (vreset! ark/*ark* (assoc-rolon! rolon-uuid rolon))))

(defn update-property-!
  [journal-entry-uuid rolon-uuid property-name property-value]
  (update-properties-! journal-entry-uuid rolon-uuid (sorted-map property-name property-value)))

(defn je-modified!
  "track the rolons modified by the journal entry"
  [journal-entry-uuid rolon-uuid]
  (let [je-value (ark/get-current-rolon-value @ark/*ark* journal-entry-uuid)
        je-property-values (::property-values je-value)
        modified (:descriptor/modified je-property-values)
        modified (if modified
                   (conj modified rolon-uuid)
                   (sorted-set rolon-uuid))]
    (update-property-! journal-entry-uuid journal-entry-uuid :descriptor/modified modified)))

(defn destroy-rolon!
  [rolon-uuid]
  (let [je-uuid (::active-journal-entry-uuid @ark/*ark*)
        rolon (ark/get-rolon @ark/*ark* rolon-uuid)
        rolon-value (ark/get-current-rolon-value @ark/*ark* rolon-uuid)
        old-property-values (::property-values rolon-value)
        property-values (reduce #(assoc %1 %2 nil) (sorted-map) (keys old-property-values))
        _ (vreset! ark/*ark* (ark/make-index-rolon @ark/*ark* rolon-uuid property-values old-property-values))
        rolon-value (assoc rolon-value ::property-values property-values)
        pjes (::property-journal-entry-uuids rolon-value)
        pjes (reduce #(assoc %1 %2 je-uuid) (sorted-map) (keys pjes))
        rolon-value (assoc rolon-value ::property-journal-entry-uuids pjes)
        rolon (assoc-in rolon [::rolon-values je-uuid] rolon-value)]
    (assoc-rolon! rolon-uuid rolon)
    (je-modified! je-uuid rolon-uuid)))

(defn update-properties!
  [rolon-uuid properties]
  (let [journal-entry-uuid (::active-journal-entry-uuid @ark/*ark*)]
    (update-properties-! journal-entry-uuid rolon-uuid properties)
    (je-modified! journal-entry-uuid rolon-uuid)))

(defn get-property-values
  [rolon-value]
  (::property-values rolon-value))

(defn get-property-journal-entry-uuids
  [rolon-value]
  (::property-journal-entry-uuids rolon-value))

(defn create-rolon-value
  "returns a new rolon value"
  [je-uuid rolon-uuid ps]
  (let [rolon-value (ark/->Rolon-value je-uuid rolon-uuid
                                       get-property-values get-property-journal-entry-uuids)
        rolon-value (assoc rolon-value ::property-values ps)
        rolon-value (assoc rolon-value ::property-journal-entry-uuids
                      (update-property-journal-entry-uuids (sorted-map) ps je-uuid))]
    rolon-value))

(defn get-rolon-values
  [rolon]
  (::rolon-values rolon))

(defn get-rolon
  [ark uuid]
  (cond
    (ark/journal-entry-uuid? uuid) ((::journal-entries ark) uuid)
    (ark/index-uuid? uuid) ((::indexes ark) uuid)
    (ark/random-uuid? uuid) ((::random-rolons ark) uuid)
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn get-journal-entries
  []
  (::journal-entries @ark/*ark*))

(defn get-indexes
  []
  (::indexes @ark/*ark*))

(defn get-random-rolons
  []
  (::random-rolons @ark/*ark*))

(defn make-rolon!
  [ark rolon-uuid properties]
  (vreset! ark/*ark* ark)
  (if (get-rolon @ark/*ark* rolon-uuid)
    (update-properties! rolon-uuid properties)
    (let [je-uuid (::active-journal-entry-uuid @ark/*ark*)
          rolon (ark/->Rolon rolon-uuid get-rolon-values)
          rolon (assoc rolon ::rolon-values
                             (sorted-map je-uuid
                                         (create-rolon-value je-uuid
                                                             rolon-uuid
                                                             properties)))]
      (assoc-rolon! rolon-uuid rolon)
      (je-modified! je-uuid rolon-uuid)
      (vreset! ark/*ark* (ark/make-index-rolon @ark/*ark* rolon-uuid properties (sorted-map))))))

(defn select-time!
  [je-uuid]
  (let [je-uuid (key (first (rsubseq (get-journal-entries) <= je-uuid)))
        ark (assoc @ark/*ark* ::selected-time je-uuid)]
    (vreset! ark/*ark* (assoc ark ::active-journal-entry-uuid je-uuid))))

(defn get-selected-time
  []
  (::selected-time @ark/*ark*))

(defn create-ark
  []
  (let [ark (ark/->Ark get-rolon get-journal-entries get-indexes get-random-rolons
                       make-rolon! destroy-rolon! update-properties!
                       get-current-journal-entry-uuid
                       select-time! get-selected-time)
        ark (assoc ark ::journal-entries (sorted-map))
        ark (assoc ark ::indexes (sorted-map))
        ark (assoc ark ::random-rolons {})]
    ark))

(defn update-ark
  [ark registry je-uuid transaction-name s]
  (let [f (registry transaction-name)
        ark (assoc ark ::latest-journal-entry-uuid je-uuid)
        ark (assoc ark ::active-journal-entry-uuid je-uuid)
        ark (ark/ark-binder ark
                            (fn []
                              (vreset! ark/*ark* (ark/make-rolon @ark/*ark* je-uuid
                                                  {:classifier/transaction-name transaction-name
                                                   :descriptor/transaction-argument s}))
                              (f s)))]
    (if (::selected-time ark)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark))

(defrecord Db [ark-atom registry-atom]
  ark/Ark-db
  (get-ark [this]
    @ark-atom)
  (register-transaction! [this transaction-name f]
    (swap! registry-atom #(assoc % transaction-name f)))
  (process-transaction! [this transaction-name s]
    (let [je-uuid (ark/journal-entry-uuid)]
      (swap! ark-atom update-ark @registry-atom je-uuid transaction-name s)
      je-uuid))
  (process-transaction-at! [this je-uuid transaction-name s]
    (swap! ark-atom update-ark @registry-atom je-uuid transaction-name s))
  )

(defn create-ark-db
  "returns an ark db"
  []
  (let [ark (create-ark)
        ark-atom (atom ark)
        registry-atom (atom (sorted-map))
        ark-db (->Db ark-atom registry-atom)]
    ark-db))
