(ns simpleArk.miView
  (:require [simpleArk.mapish :as mapish])
  (:import (clojure.lang Reversible
                         Seqable
                         ILookup
                         IPersistentCollection
                         Associative)))

(set! *warn-on-reflection* true)

(declare ->MI-view)

(deftype MI-view [ark-value rolon-uuid all-changes]
  ILookup
  (valAt [this property-path default]
    (let [changes (get all-changes property-path)]
      (if (nil? changes)
        default
        (let [changes (mapish/mi-sub changes nil nil <= (mapish/get-selected-time ark-value))
              fst (first (rseq changes))]
          (if (nil? fst)
            default
            (val fst))))))
  (valAt [this property-path]
    (get this property-path nil))
  Associative
  (containsKey [this path]
    (some? (.valAt this path)))
  (entryAt [this path]
    (let [value (.valAt this path)]
        (clojure.lang.MapEntry. path value)))
  Seqable
  (seq [this]
    (map
      #(clojure.lang.MapEntry. (key %) (val (val %)))
      (filter
        #(some? (val %))
        (map
          #(clojure.lang.MapEntry.
            (key %)
            (first (rseq (mapish/mi-sub (val %) nil nil <= (mapish/get-selected-time ark-value)))))
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
            (first (rseq (mapish/mi-sub (val %) nil nil <= (mapish/get-selected-time ark-value)))))
          (rseq all-changes)))))
  mapish/MI
  (mi-sub [this prefix]
    (->MI-view ark-value
               rolon-uuid
               (mapish/mi-sub all-changes prefix)))
  (mi-sub [this start-test start-key end-test end-key]
    (->MI-view ark-value
               rolon-uuid
               (mapish/mi-sub all-changes start-test start-key end-test end-key)))
  )

(defmethod print-method MI-view [v ^java.io.Writer w]
  (.write w (prn-str "#miMap/MI-map { "))
  (reduce (fn [_ i] (.write w (prn-str (key i) (val i) ""))) nil (seq v))
  (.write w (prn-str "}")))
