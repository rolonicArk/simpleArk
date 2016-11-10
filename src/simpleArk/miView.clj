(ns simpleArk.miView
  (:require [simpleArk.mapish :as mapish])
  (:import (clojure.lang Reversible
                         Seqable
                         ILookup
                         IPersistentCollection
                         Associative
                         IPersistentVector
                         PersistentTreeMap)))

(set! *warn-on-reflection* true)

(deftype MI-view [ark-value rolon-uuid all-changes get-selected-time get-property-values]
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
                         (mapish/mi-sub all-changes start-test start-key end-test end-key)))
  )
