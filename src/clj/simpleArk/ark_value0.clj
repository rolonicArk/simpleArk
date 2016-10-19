(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.uuid :as uuid]
            [simpleArk.mapish :as mapish]
            [simpleArk.vecish :as vecish]))

(set! *warn-on-reflection* true)

;todo drop
(defn $to-names
  [properties]
  (reduce
    #(mapish/mi-assoc %1 (first (:v (key %2))) (val %2))
    (mapish/->MI-map (sorted-map) nil nil nil nil)
    (mapish/mi-seq properties)))

;todo drop
(defn $to-paths
  [properties]
  (reduce
    #(mapish/mi-assoc %1 (vecish/->Vecish [(key %2)]) (val %2))
    (mapish/->MI-map (sorted-map) nil nil nil nil)
    (mapish/mi-seq properties)))

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
    (uuid/journal-entry-uuid? uuid) (mapish/mi-get (ark-value/get-journal-entries ark-value) uuid)
    (uuid/index-uuid? uuid) (mapish/mi-get (ark-value/get-indexes ark-value) uuid)
    (uuid/random-uuid? uuid) (mapish/mi-get (ark-value/get-random-rolons ark-value) uuid)
    :else (throw (Exception. (str uuid " was not recognized")))))

(defn assoc-rolon
  "update the ark with the revised/new rolon"
  [ark-value rolon-uuid rolon]
  (cond
    (uuid/journal-entry-uuid? rolon-uuid)
    (let [journal-entries (get ark-value ::journal-entries)
          journal-entries (mapish/mi-assoc journal-entries rolon-uuid rolon)]
      (assoc ark-value ::journal-entries journal-entries))
    (uuid/index-uuid? rolon-uuid)
    (let [indexes (get ark-value ::indexes)
          indexes (mapish/mi-assoc indexes rolon-uuid rolon)]
      (assoc ark-value ::indexes indexes))
    (uuid/random-uuid? rolon-uuid)
    (let [rolons (get ark-value ::random-rolons)
          rolons (mapish/mi-assoc rolons rolon-uuid rolon)]
      (assoc ark-value ::random-rolons rolons))
    :else (throw (Exception. (str rolon-uuid " is unrecognized")))))

(defn update-property-changes
  [property-changes je-uuid new-value]
  (let [property-changes (if (some? property-changes)
                           property-changes
                           (mapish/->MI-map
                             (sorted-map)
                             nil nil nil nil))
        first-entry (first (mapish/mi-seq property-changes))]
    (if (or (nil? first-entry) (not= new-value (val first-entry)))
      (mapish/mi-assoc property-changes je-uuid new-value)
      property-changes)))

(defn update-changes-by-property
  ([changes-by-property je-uuid changed-properties]
   (reduce #(update-changes-by-property %1 je-uuid (key %2) (val %2))
           changes-by-property
           (mapish/mi-seq changed-properties)))
  ([changes-by-property je-uuid property-name new-value]
   (let [changes-by-property (if (some? changes-by-property)
                               changes-by-property
                               (mapish/->MI-map
                                 (sorted-map)
                                 nil nil nil nil))]
     (mapish/mi-assoc changes-by-property
                      property-name
                      (update-property-changes (mapish/mi-get changes-by-property property-name)
                                               je-uuid
                                               new-value)))))

(defn update-properties-
  [ark-value journal-entry-uuid rolon-uuid properties]
  (let [rolon (ark-value/get-rolon ark-value rolon-uuid)
        rolon (assoc rolon ::changes-by-property
                           (update-changes-by-property (::changes-by-property rolon)
                                                       journal-entry-uuid
                                                       ($to-paths properties)))
        property-values (ark-value/$get-property-values ark-value rolon-uuid)
        ark-value (ark-value/$make-index-rolon ark-value
                                               rolon-uuid
                                               ($to-paths properties)
                                               property-values)]
    (assoc-rolon ark-value rolon-uuid rolon)))

(defn update-property-
  [ark-value journal-entry-uuid rolon-uuid property-name property-value]
  (update-properties- ark-value
                      journal-entry-uuid
                      rolon-uuid
                      (mapish/->MI-map (sorted-map property-name property-value) nil nil nil nil)))

(defn je-modified
  "track the rolons modified by the journal entry"
  [ark-value rolon-uuid]
  (let [journal-entry-uuid (::active-journal-entry-uuid ark-value)
        modified (ark-value/$get-property-value ark-value
                                                journal-entry-uuid
                                                (vecish/->Vecish [:descriptor/modified]))
        modified (if modified
                   (conj modified rolon-uuid)
                   (sorted-set rolon-uuid))]
    (update-property- ark-value journal-entry-uuid journal-entry-uuid :descriptor/modified modified)))

(defn destroy-rolon
  [ark-value rolon-uuid]
  (let [je-uuid (::active-journal-entry-uuid ark-value)
        rolon (ark-value/get-rolon ark-value rolon-uuid)
        old-property-values (ark-value/$get-property-values ark-value rolon-uuid)
        property-values (reduce #(mapish/mi-assoc %1 (key %2) nil)
                                (mapish/->MI-map (sorted-map) nil nil nil nil)
                                (mapish/mi-seq old-property-values))
        rolon (assoc rolon ::changes-by-property
                            (update-changes-by-property
                              (::changes-by-property rolon)
                              je-uuid
                              property-values))
        ark-value (ark-value/$make-index-rolon ark-value
                                              rolon-uuid
                                              property-values
                                              old-property-values)
        ark-value (assoc-rolon ark-value rolon-uuid rolon)]
    (je-modified ark-value rolon-uuid)))

(defn update-properties
  [ark-value rolon-uuid properties]
  (let [journal-entry-uuid (::active-journal-entry-uuid ark-value)
        ark-value (update-properties- ark-value journal-entry-uuid rolon-uuid ($to-names properties))]
    (je-modified ark-value rolon-uuid)))

(defn select-time
  [ark-value je-uuid]
  (let [jes
        (mapish/mi-sub
          (ark-value/get-journal-entries ark-value)
          nil
          nil
          <=
          je-uuid)
        je-uuid
        (key
          (first
            (mapish/mi-rseq jes)))]
    (-> ark-value
        (assoc ::journal-entries jes)
        (assoc ::selected-time je-uuid)
        (assoc ::active-journal-entry-uuid je-uuid))))

(defn get-selected-time
  [ark-value]
  (::selected-time ark-value))

(defn get-changes-by-property
  ([rolon property-path]
   (let [ark-value (:ark-value rolon)
         changes-by-property (get-changes-by-property rolon)
         pc (mapish/mi-get changes-by-property property-path)]
     (if (nil? pc)
       nil
       (mapish/mi-sub pc nil nil <= (get-selected-time ark-value)))))
  ([rolon]
   (::changes-by-property rolon)))

(defn make-rolon
  [ark-value rolon-uuid properties]
  (if (ark-value/get-rolon ark-value rolon-uuid)
    (update-properties ark-value rolon-uuid properties)
    (let [je-uuid (::active-journal-entry-uuid ark-value)
          rolon (ark-value/->Rolon rolon-uuid
                                   get-changes-by-property
                                   ark-value)
          rolon (assoc rolon ::changes-by-property
                             (update-changes-by-property (::changes-by-property rolon)
                                                         je-uuid
                                                         properties))
          ark-value (assoc-rolon ark-value rolon-uuid rolon)
          ark-value (je-modified ark-value rolon-uuid)]
      (ark-value/$make-index-rolon ark-value
                                   rolon-uuid
                                   properties
                                   (mapish/->MI-map (sorted-map) nil nil nil nil)))))

(defn index-name-uuid
  [ark-value]
  (::index-name-uuid ark-value))

(defn update-ark
  [ark-value je-uuid transaction-name s]
  (let [ark-value (-> ark-value
                      (assoc ::latest-journal-entry-uuid je-uuid)
                      (assoc ::active-journal-entry-uuid je-uuid)
                      (ark-value/$make-rolon je-uuid
                                            (mapish/->MI-map
                                              (sorted-map
                                                (vecish/->Vecish [:classifier/transaction-name]) transaction-name
                                                (vecish/->Vecish [:descriptor/transaction-argument]) s)
                                              nil nil nil nil))
                      (ark-value/eval-transaction transaction-name s))]
    (if (::selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn create-mi
  ([] (create-mi (sorted-map)))
  ([sorted-map] (mapish/->MI-map sorted-map nil nil nil nil)))

(defn create-ark
  [this-db]
  (-> (ark-value/->Ark-value this-db get-rolon get-journal-entries get-indexes get-random-rolons
                             make-rolon destroy-rolon update-properties update-ark
                             get-current-journal-entry-uuid
                             select-time get-selected-time index-name-uuid create-mi)
      (assoc ::journal-entries (mapish/->MI-map (sorted-map) nil nil nil nil))
      (assoc ::indexes (mapish/->MI-map (sorted-map) nil nil nil nil))
      (assoc ::random-rolons (mapish/->MI-map (sorted-map) nil nil nil nil))
      (assoc ::index-name-uuid (uuid/index-uuid this-db :classifier/index.name))))

(defn- build
  "returns an ark db"
  [m]
  (assoc m :ark-value/create-ark create-ark))

(defn builder
  []
  build)
