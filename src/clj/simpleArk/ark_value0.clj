(ns simpleArk.ark-value0
  (:require [simpleArk.ark-value :as ark-value]
            [simpleArk.uuid :as uuid]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(defn create-mi
  [ark-value & keyvals]
  (apply mapish/new-MI-map keyvals))

(defn je-modified
  "track the rolons modified by the journal entry"
  [ark-value rolon-uuid]
  (let [journal-entry-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
        ark-value (ark-value/update-property- ark-value
                                    journal-entry-uuid
                                    journal-entry-uuid
                                    [:descriptor/modified rolon-uuid]
                                    true)]
    (ark-value/update-property- ark-value
                      journal-entry-uuid
                      rolon-uuid
                      [:descriptor/journal-entry journal-entry-uuid]
                      true)))

(defn update-properties
  [ark-value rolon-uuid properties]
  (let [journal-entry-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
        ark-value (ark-value/update-properties- ark-value journal-entry-uuid rolon-uuid properties)]
    (je-modified ark-value rolon-uuid)))

(defn destroy-rolon
  [ark-value rolon-uuid]
  (let [old-property-values (ark-value/get-property-values ark-value rolon-uuid)
        property-values (reduce #(assoc %1 (key %2) nil)
                                (create-mi ark-value)
                                (seq old-property-values))
        ark-value (ark-value/make-index-rolon ark-value
                                              rolon-uuid
                                              property-values
                                              old-property-values)
        je-uuid (ark-value/get-latest-journal-entry-uuid ark-value)
        rolon (ark-value/get-rolon ark-value rolon-uuid)
        rolon (ark-value/update-rolon-properties ark-value rolon je-uuid property-values)
        ark-value (ark-value/assoc-rolon ark-value rolon-uuid rolon)]
    (je-modified ark-value rolon-uuid)))

(defn make-rolon
  [ark-value rolon-uuid properties]
  (let [ark-value
        (if (ark-value/get-rolon ark-value rolon-uuid)
          ark-value
          (ark-value/assoc-rolon
            ark-value
            rolon-uuid
            (ark-value/->Rolon rolon-uuid)))]
    (update-properties ark-value rolon-uuid properties)))

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
  (-> (ark-value/->Ark-value this-db make-rolon destroy-rolon update-properties update-ark create-mi)
      (ark-value/init-ark-value)))

(defn- build
  "returns an ark db"
  [m]
  (assoc m :ark-value/create-ark create-ark))

(defn builder
  []
  build)
