(ns simpleArk.impl0
  (:require [simpleArk.core :as ark]
            [simpleArk.log :as log]
            [simpleArk.uuid :as uuid]))

(defn get-current-journal-entry-uuid
  []
  (::active-journal-entry-uuid @ark/*ark*))

(defn update-property-journal-entry-uuids
  "where pjes is to be updated and ps are the new property values"
  [pjes ps je-uuid]
  (reduce #(assoc %1 %2 je-uuid) pjes (keys ps)))

(defn assoc-rolon!
  "update the ark with the revised/new rolon"
  [rolon-uuid rolon]
  (vreset! ark/*ark* (cond
                       (uuid/journal-entry-uuid? rolon-uuid)
                       (assoc-in @ark/*ark* [::journal-entries rolon-uuid] rolon)
                       (uuid/index-uuid? rolon-uuid)
                       (assoc-in @ark/*ark* [::indexes rolon-uuid] rolon)
                       (uuid/random-uuid? rolon-uuid)
                       (assoc-in @ark/*ark* [::random-rolons rolon-uuid] rolon)
                       :else (throw (Exception. (str rolon-uuid " is unrecognized"))))))

(defn get-rolon
  [uuid]
  (cond
    (uuid/journal-entry-uuid? uuid) ((::journal-entries @ark/*ark*) uuid)
    (uuid/index-uuid? uuid) ((::indexes @ark/*ark*) uuid)
    (uuid/random-uuid? uuid) ((::random-rolons @ark/*ark*) uuid)
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn update-properties-!
  [journal-entry-uuid rolon-uuid properties]
  (let [rolon (get-rolon rolon-uuid)
        rolon-value (ark/get-rolon-value-at rolon-uuid)
        property-values (::property-values rolon-value)
        _ (ark/make-index-rolon! rolon-uuid properties property-values)
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
  (let [je-value (ark/get-rolon-value-at journal-entry-uuid)
        je-property-values (::property-values je-value)
        modified (:descriptor/modified je-property-values)
        modified (if modified
                   (conj modified rolon-uuid)
                   (sorted-set rolon-uuid))]
    (update-property-! journal-entry-uuid journal-entry-uuid :descriptor/modified modified)))

(defn destroy-rolon!
  [rolon-uuid]
  (let [je-uuid (::active-journal-entry-uuid @ark/*ark*)
        rolon (get-rolon rolon-uuid)
        rolon-value (ark/get-rolon-value-at rolon-uuid)
        old-property-values (::property-values rolon-value)
        property-values (reduce #(assoc %1 %2 nil) (sorted-map) (keys old-property-values))
        _ (ark/make-index-rolon! rolon-uuid property-values old-property-values)
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
  [rolon-uuid properties]
  (if (get-rolon rolon-uuid)
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
      (ark/make-index-rolon! rolon-uuid properties (sorted-map)))))

(defn select-time!
  [je-uuid]
  (let [je-uuid (key (first (rsubseq (get-journal-entries) <= je-uuid)))
        ark (assoc @ark/*ark* ::selected-time je-uuid)]
    (vreset! ark/*ark* (assoc ark ::active-journal-entry-uuid je-uuid))))

(defn get-selected-time
  []
  (::selected-time @ark/*ark*))

(defn index-name-uuid
  []
  (::index-name-uuid @ark/*ark*))

(defn update-ark
  [ark registry je-uuid transaction-name s]
  (let [f (registry transaction-name)
        ark (assoc ark ::latest-journal-entry-uuid je-uuid)
        ark (assoc ark ::active-journal-entry-uuid je-uuid)
        ark (ark/ark-binder ark
                            (fn []
                              (vreset! ark/*ark* (ark/make-rolon! je-uuid
                                                                  {:classifier/transaction-name transaction-name
                                                   :descriptor/transaction-argument s}))
                              (f s)))]
    (if (::selected-time ark)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark))

(defn get-ark
  [ark-db]
  @(::ark-atom ark-db))

(defn register-transaction!
  [ark-db transaction-name f]
  (swap! (::registry-atom ark-db) #(assoc % transaction-name f)))

(defn process-transaction!
  [ark-db transaction-name s]
  (let [je-uuid (uuid/journal-entry-uuid ark-db)]
    (swap! (::ark-atom ark-db) update-ark @(::registry-atom ark-db) je-uuid transaction-name s)
    (log/info! ark-db :transaction transaction-name s)
    je-uuid))

(defn process-transaction-at!
  [ark-db je-uuid transaction-name s]
  (swap! (::ark-atom ark-db) update-ark @(::registry-atom ark-db) je-uuid transaction-name s)
  (log/info! ark-db :transaction transaction-name s))

(defn open-ark
  [ark-db]
  (let [ark (ark/->Ark ark-db get-rolon get-journal-entries get-indexes get-random-rolons
                       make-rolon! destroy-rolon! update-properties!
                       get-current-journal-entry-uuid
                       select-time! get-selected-time index-name-uuid)
        ark (assoc ark ::journal-entries (sorted-map))
        ark (assoc ark ::indexes (sorted-map))
        ark (assoc ark ::random-rolons {})
        ark (assoc ark ::index-name-uuid (uuid/index-uuid ark-db :classifier/index.name))]
    (reset! (::ark-atom ark-db) ark)
    ark))

(defn build
  "returns an ark db"
  [m]
  (let [ark-atom (atom nil)
        registry-atom (atom (sorted-map))
        ark-db (-> m
                   (assoc ::ark-atom ark-atom)
                   (assoc ::registry-atom registry-atom)
                   (assoc :ark-db/open-ark open-ark)
                   (assoc :ark-db/get-ark get-ark)
                   (assoc :ark-db/register-transaction! register-transaction!)
                   (assoc :ark-db/process-transaction! process-transaction!)
                   (assoc :ark-db/process-transaction-at! process-transaction-at!)
                   )]
    ark-db))

(defn builder
  []
  (fn [m]
    (let [ark-db (build m)]
      (ark/open-ark ark-db)
      ark-db)))
