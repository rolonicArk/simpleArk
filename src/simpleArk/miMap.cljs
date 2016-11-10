(ns simpleArk.miMap
  (:require [simpleArk.mapish :as mapish]))

(declare ->MI-map)

(defn new-MI-map
      [& keyvals]
      (let [m (apply sorted-map-by mapish/vec-comp keyvals)]
           (->MI-map m nil nil nil nil)))

(defn create-map
      [m]
      (let [m (into (sorted-map-by mapish/vec-comp) m)]
           (->MI-map m nil nil nil nil)))

(cljs.reader/register-tag-parser! "miMap/MI-map" create-map)

(deftype MI-map [sorted-map start-test start-path end-test end-path]
         ISeqable
         (-seq [this]
               (cond
                 (empty? sorted-map)
                 nil
                 (or start-path end-path)
                 (let [start-test (if start-path
                                    start-test
                                    >=)
                       start-path (if start-path
                                    start-path
                                    (key (first sorted-map)))
                       end-test (if end-path
                                  end-test
                                  <=)
                       end-path (if end-path
                                  end-path
                                  (key (last sorted-map)))]
                      (subseq sorted-map start-test start-path end-test end-path))
                 :else
                 (seq sorted-map)))
         IReversible
         (-rseq [coll]
                (cond
                  (empty? sorted-map)
                  nil
                  (or start-path end-path)
                  (let [start-test (if start-path
                                     start-test
                                     >=)
                        start-path (if start-path
                                     start-path
                                     (key (first sorted-map)))
                        end-test (if end-path
                                   end-test
                                   <=)
                        end-path (if end-path
                                   end-path
                                   (key (last sorted-map)))]
                       (rsubseq sorted-map start-test start-path end-test end-path))
                  :else
                  (rseq sorted-map)))
         IPrintWithWriter
         (-pr-writer [^MI-map coll writer opts]
                     (let [pr-pair (fn [keyval]
                                       (pr-sequential-writer writer pr-writer "" " " "" opts keyval))]
                          (pr-sequential-writer writer pr-pair
                                                "#miMap/MI-map {"
                                                ", "
                                                "}"
                                                opts (seq coll))))
         IAssociative
         (-contains-key? [this path]
                      (and (mapish/in-range path start-test start-path end-test end-path)
                           (contains? sorted-map path)))
         (-assoc [this path value]
                (if (mapish/in-range path start-test start-path end-test end-path)
                  (->MI-map
                    (assoc sorted-map path value)
                    start-test start-path end-test end-path)
                  this))
         ICollection
         (-conj [this entry]
                (if (vector? entry)
                  (-assoc this (-nth entry 0) (-nth entry 1))
                  (reduce -conj this entry)))
         ILookup
         (-lookup [this key]
                (if (mapish/in-range key start-test start-path end-test end-path)
                  (get sorted-map key)
                  nil))
         (-lookup [this key not-found]
                (if (mapish/in-range key start-test start-path end-test end-path)
                  (get sorted-map key not-found)
                  not-found))
         mapish/MI
         (mi-sub
           [this prefix]
           (let [[s-test s-path e-test e-path]
                 (mapish/mi-munge prefix start-test start-path end-test end-path)]
                (if (and
                      (= s-test start-test)
                      (= e-test end-test)
                      (= 0 (compare s-path start-path))
                      (= 0 (compare e-path end-path)))
                  this
                  (->MI-map sorted-map s-test s-path e-test e-path))))
         (mi-sub
           [this stest spath etest epath]
           (let [[s-test s-path e-test e-path]
                 (mapish/mi-munge start-test start-path end-test end-path stest spath etest epath)]
                (if (and
                      (= s-test start-test)
                      (= e-test end-test)
                      (= 0 (compare s-path start-path))
                      (= 0 (compare e-path end-path)))
                  this
                  (->MI-map sorted-map s-test s-path e-test e-path)))))
