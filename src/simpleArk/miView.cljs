(ns simpleArk.miView
  (:require [simpleArk.mapish :as mapish]))

(declare ->MI-view)

(deftype MI-view [ark-value rolon-uuid all-changes selected-time]
         ISeqable
         (-seq [this]
               (map
                 #(vec [(key %) (val (val %))])
                 (filter
                   #(some? (val %))
                   (map
                     #(vec [(key %) (first (rseq (mapish/mi-sub (val %) nil nil <= selected-time)))])
                     (seq all-changes)))))
         IReversible
         (-rseq [this]
                (map
                  #([(key %) (val (val %))])
                  (filter
                    #(some? (val %))
                    (map
                      #([(key %) (first (rseq (mapish/mi-sub (val %) nil nil <= selected-time)))])
                      (rseq all-changes)))))
         IPrintWithWriter
         (-pr-writer [this writer opts]
                     (let [pr-pair (fn [keyval]
                                       (pr-sequential-writer writer pr-writer "" " " "" opts keyval))]
                          (pr-sequential-writer writer pr-pair
                                                "#miMap/MI-map {"
                                                ", "
                                                "}"
                                                opts this)))
         ILookup
         (-lookup [this path not-found]
                  (let [changes (get all-changes path)]
                       (if (nil? changes)
                         not-found
                         (let [changes (mapish/mi-sub changes nil nil <= selected-time)
                               fst (first (rseq changes))]
                              (if (nil? fst)
                                not-found
                                (-nth fst 1))))))
         (-lookup [this path]
                  (get this path nil))
         IAssociative
         (-contains-key? [this path]
                         (some? (.valAt this path)))
         mapish/MI
         (mi-sub [this prefix]
                 (->MI-view ark-value
                            rolon-uuid
                            (mapish/mi-sub all-changes prefix)
                            selected-time))
         (mi-sub [this stest spath etest epath]
                 (->MI-view ark-value
                            rolon-uuid
                            (mapish/mi-sub all-changes stest spath etest epath)
                            selected-time))
         )
