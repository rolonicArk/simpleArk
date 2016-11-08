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

(deftype MI-map [sorted-map start-test start-path end-test end-path]
         IPrintWithWriter
         (-pr-writer [^MI-map coll writer opts]
                     (let [pr-pair (fn [keyval] (pr-sequential-writer writer pr-writer "" " " "" opts keyval))]
                          (pr-sequential-writer writer pr-pair
                                                "#miMap/MI-map {"
                                                ", "
                                                "}"
                                                opts sorted-map)))
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
