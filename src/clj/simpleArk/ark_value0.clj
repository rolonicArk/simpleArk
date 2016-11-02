(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.uuid :as uuid]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(defn assoc-rolon
  "update the ark with the revised/new rolon"
  [ark-value rolon-uuid rolon]
  (cond
    (uuid/journal-entry-uuid? rolon-uuid)
    (let [journal-entries (ark-value/get-journal-entries ark-value)
          journal-entries (assoc journal-entries [rolon-uuid] rolon)]
      (assoc ark-value :journal-entries journal-entries))
    (uuid/index-uuid? rolon-uuid)
    (let [indexes (ark-value/get-indexes ark-value)
          indexes (assoc indexes [rolon-uuid] rolon)]
      (assoc ark-value :indexes indexes))
    (uuid/random-uuid? rolon-uuid)
    (let [rolons (ark-value/get-random-rolons ark-value)
          rolons (assoc rolons [rolon-uuid] rolon)]
      (assoc ark-value :random-rolons rolons))
    :else (throw (Exception. (str rolon-uuid " is unrecognized")))))

(defn create-mi
  [ark-value & keyvals]
  (apply mapish/new-MI-map keyvals))

(defn update-property-changes
  [ark-value property-changes je-uuid new-value]
  (let [property-changes (if (some? property-changes)
                           property-changes
                           (create-mi ark-value))
        first-entry (first (seq property-changes))]
    (if (or (nil? first-entry) (not= new-value (val first-entry)))
      (assoc property-changes [je-uuid] new-value)
      property-changes)))

(defn update-changes-by-property
  ([ark-value changes-by-property je-uuid changed-properties]
   (reduce #(update-changes-by-property ark-value %1 je-uuid (key %2) (val %2))
           changes-by-property
           (seq changed-properties)))
  ([ark-value changes-by-property je-uuid property-name new-value]
   (let [changes-by-property (if (some? changes-by-property)
                               changes-by-property
                               (create-mi ark-value))]
     (assoc changes-by-property
                      property-name
                      (update-property-changes ark-value
                                               (get changes-by-property property-name)
                                               je-uuid
                                               new-value)))))

(defn update-properties-
  [ark-value journal-entry-uuid rolon-uuid properties]
  (let [rolon (ark-value/get-rolon ark-value rolon-uuid)
        rolon (assoc rolon ::changes-by-property
                           (update-changes-by-property ark-value
                                                       (::changes-by-property rolon)
                                                       journal-entry-uuid
                                                       properties))
        property-values (ark-value/get-property-values ark-value rolon-uuid)
        ark-value (ark-value/make-index-rolon ark-value
                                               rolon-uuid
                                               properties
                                               property-values)]
    (assoc-rolon ark-value rolon-uuid rolon)))

(defn update-property-
  [ark-value journal-entry-uuid rolon-uuid property-path property-value]
  (update-properties- ark-value
                      journal-entry-uuid
                      rolon-uuid
                      (create-mi ark-value property-path property-value)))

(defn je-modified
  "track the rolons modified by the journal entry"
  [ark-value rolon-uuid]
  (let [journal-entry-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
        ark-value (update-property- ark-value
                                    journal-entry-uuid
                                    journal-entry-uuid
                                    [:descriptor/modified rolon-uuid]
                                    true)]
    (update-property- ark-value
                      journal-entry-uuid
                      rolon-uuid
                      [:descriptor/journal-entry journal-entry-uuid]
                      true)))

(defn destroy-rolon
  [ark-value rolon-uuid]
  (let [je-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
        rolon (ark-value/get-rolon ark-value rolon-uuid)
        old-property-values (ark-value/get-property-values ark-value rolon-uuid)
        property-values (reduce #(assoc %1 (key %2) nil)
                                (create-mi ark-value)
                                (seq old-property-values))
        rolon (assoc rolon ::changes-by-property
                            (update-changes-by-property
                              ark-value
                              (::changes-by-property rolon)
                              je-uuid
                              property-values))
        ark-value (ark-value/make-index-rolon ark-value
                                              rolon-uuid
                                              property-values
                                              old-property-values)
        ark-value (assoc-rolon ark-value rolon-uuid rolon)]
    (je-modified ark-value rolon-uuid)))

(defn update-properties
  [ark-value rolon-uuid properties]
  (let [journal-entry-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
        ark-value (update-properties- ark-value journal-entry-uuid rolon-uuid properties)]
    (je-modified ark-value rolon-uuid)))

(defn select-time
  [ark-value je-uuid]
  (let [jes
        (mapish/mi-sub
          (ark-value/get-journal-entries ark-value)
          nil
          nil
          <=
          [je-uuid])
        je-uuid
        (key
          (first
            (rseq jes)))]
    (assoc ark-value :selected-time je-uuid)))

(defn get-changes-by-property
  ([rolon property-path]
   (let [ark-value (:ark-value rolon)
         changes-by-property (get-changes-by-property rolon)
         pc (get changes-by-property property-path)]
     (if (nil? pc)
       nil
       (mapish/mi-sub pc nil nil <= (ark-value/get-selected-time ark-value)))))
  ([rolon]
   (::changes-by-property rolon)))

(defn make-rolon
  [ark-value rolon-uuid properties]
  (if (ark-value/get-rolon ark-value rolon-uuid)
    (update-properties ark-value rolon-uuid properties)
    (let [je-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
          rolon (ark-value/->Rolon rolon-uuid
                                   get-changes-by-property
                                   ark-value)
          rolon (assoc rolon ::changes-by-property
                             (update-changes-by-property ark-value
                                                         (::changes-by-property rolon)
                                                         je-uuid
                                                         properties))
          ark-value (assoc-rolon ark-value rolon-uuid rolon)
          ark-value (je-modified ark-value rolon-uuid)]
      (ark-value/make-index-rolon ark-value
                                   rolon-uuid
                                   properties
                                   (create-mi ark-value)))))

(defn update-ark
  [ark-value je-uuid transaction-name s]
  (let [ark-value (-> ark-value
                      (assoc :latest-journal-entry-uuid je-uuid)
                      (ark-value/make-rolon je-uuid
                                            (create-mi
                                              ark-value
                                              [:classifier/transaction-name] transaction-name
                                              [:descriptor/transaction-argument] s))
                      (ark-value/eval-transaction transaction-name s))]
    (if (:selected-time ark-value)
      (throw (Exception. "Transaction can not update ark with a selected time")))
    ark-value))

(defn create-ark
  [this-db]
  (-> (ark-value/->Ark-value this-db make-rolon destroy-rolon update-properties update-ark
                             select-time create-mi)
      (ark-value/init-ark-value)))

(defn- build
  "returns an ark db"
  [m]
  (assoc m :ark-value/create-ark create-ark))

(defn builder
  []
  build)
