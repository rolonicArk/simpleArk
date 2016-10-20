(ns simpleArk.mapish
  (:require [simpleArk.vecish :as vecish]))

(set! *warn-on-reflection* true)

(defprotocol MI
  (mi-get [this key] [this key default])
  (mi-seq [this])
  (mi-rseq [this])
  (mi-sub [this prefix start-test start-key end-test end-key]))

(defprotocol MIU
  (mi-assoc [this key value]))

(defn in-range [path prefix stest spath etest epath]
  (let [sc (compare path spath)
        ec (compare path epath)]
    (and
      (vecish/prefixed? path prefix)
      (cond
        (nil? spath)
        true
        (> 0 sc)
        false
        (< 0 sc)
        true
        :else
        (= stest >=))
      (cond
        (nil? epath)
        true
        (< 0 ec)
        false
        (> 0 ec)
        true
        :else
        (= etest <=)))))

(defn munge
  ([prefix start-test start-path end-test end-path]
   (if (nil? prefix)
     [start-test start-path end-test end-path]
     (munge start-test start-path end-test end-path
            >= prefix < (conj prefix nil))))
  ([start-test start-path end-test end-path stest spath etest epath]
   (let [sc (compare spath start-path)
         s-test (cond
                  (nil? spath)
                  start-test
                  (nil? start-path)
                  stest
                  (< sc 0)
                  start-test
                  (> sc 0)
                  stest
                  (= stest start-test)
                  start-test
                  (= stest >)
                  stest
                  :else
                  start-test)
         s-path (cond
                  (nil? spath)
                  start-path
                  (nil? start-path)
                  spath
                  (< sc 0)
                  start-path
                  (> sc 0)
                  spath
                  (= stest start-test)
                  start-path
                  (= stest >)
                  spath
                  :else
                  start-path)
         ec (compare epath end-path)
         e-test (cond
                  (nil? epath)
                  end-test
                  (nil? end-path)
                  etest
                  (> ec 0)
                  end-test
                  (< ec 0)
                  etest
                  (= etest end-test)
                  end-test
                  (= etest <)
                  etest
                  :else
                  end-test)
         e-path (cond
                  (nil? epath)
                  end-path
                  (nil? end-path)
                  epath
                  (> ec 0)
                  end-path
                  (< ec 0)
                  epath
                  (= etest end-test)
                  end-path
                  (= etest <)
                  epath
                  :else
                  end-path)]
     [s-test s-path e-test e-path])))

(declare ->MI-map)

(deftype MI-map [sorted-map prefix start-test start-path end-test end-path]
  MI
  (mi-get [this key]
    (if (in-range key prefix start-test start-path end-test end-path)
      (get sorted-map key)
      nil))
  (mi-get [this key not-found]
    (if (in-range key prefix start-test start-path end-test end-path)
      (get sorted-map key not-found)
      not-found))
  (mi-seq [this]
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
                      (key (last sorted-map)))
            [start-test start-path end-test end-path]
            (munge prefix start-test start-path end-test end-path)]
        (subseq sorted-map start-test start-path end-test end-path))
      :else
      (seq sorted-map)))
  (mi-rseq [this]
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
                      (key (last sorted-map)))
            [start-test start-path end-test end-path]
            (munge prefix start-test start-path end-test end-path)]
        (rsubseq sorted-map start-test start-path end-test end-path))
      :else
      (rseq sorted-map)))
  (mi-sub [this pf stest spath etest epath]
    (let [[s-test s-key e-test e-key]
          (munge start-test start-path end-test end-path stest spath etest epath)
          [s-test s-key e-test e-key]
          (munge pf s-test s-key e-test e-key)]
      (if (and
            (= s-test start-test)
            (= e-test end-test)
            (= 0 (compare s-key start-path))
            (= 0 (compare e-key end-path)))
        this
        (->MI-map sorted-map prefix s-test s-key e-test e-key))))

  MIU
  (mi-assoc [this key value]
    (if (in-range key prefix start-test start-path end-test end-path)
      (->MI-map
        (assoc sorted-map key value)
        prefix start-test start-path end-test end-path)
      this)))
