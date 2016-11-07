(ns simpleArk.mapish
  (:import (clojure.lang Reversible
                         Seqable
                         ILookup
                         IPersistentCollection
                         Associative
                         IPersistentVector)))

(set! *warn-on-reflection* true)

(defn index?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "index" (namespace kw)))))

(defn bi-rel?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "bi-rel" (namespace kw)))))

(defn rel?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "rel" (namespace kw)))))

(defn inv-rel?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "inv-rel" (namespace kw)))))

(defn content?
  [kw]
  (and (keyword? kw)
       (= 0 (compare "content" (namespace kw)))))

(defn validate-property-path
  [property-path]
  (let [kw (first property-path)]
    (if (or (index? kw) (bi-rel? kw) (rel? kw) (inv-rel? kw))
      (if (< 1 (count  property-path))
        (throw (Exception. (str property-path " has too many elements for a classifier"))))
      (if (not (content? kw))
        (throw (Exception. (str property-path " is neither a classifier nor a descriptor")))))))

(defn validate-properties
  [properties]
  (reduce (fn [_ e]
            (let [path (key e)
                  kw (first path)
                  v (val e)]
              (validate-property-path path)
              (if (and (or (bi-rel? kw) (rel? kw) (inv-rel? kw))
                       (not (uuid? v)))
                (throw (Exception. (str path " is not assigned a UUID"))))))
          nil
          (seq properties)))

(defprotocol MI
  (mi-sub [this prefix] [this start-test start-key end-test end-key]))

(defn vec-comp [a b]
  (let [ac (count a)
        bc (count b)
        minc (min ac bc)]
    (loop [i 0]
      (if (>= i minc)
        (compare ac bc)
        (let [ai (a i)
              bi (b i)
              r (compare ai bi)]
          (if (not= r 0)
            (if (nil? ai)
              1
              (if (nil? bi)
                -1
                r))
            (recur (+  i 1))))))))

(defn in-range [path stest spath etest epath]
  (let [sc (vec-comp path spath)
        ec (vec-comp path epath)]
    (and
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

(defn mi-munge
  ([prefix start-test start-path end-test end-path]
   (if (nil? prefix)
     [start-test start-path end-test end-path]
     (mi-munge start-test start-path end-test end-path
               >= prefix < (conj prefix nil))))
  ([start-test start-path end-test end-path stest spath etest epath]
   (let [sc (vec-comp spath start-path)
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
         ec (vec-comp epath end-path)
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

(defn new-MI-map [& keyvals]
  (let [m (apply sorted-map-by vec-comp keyvals)]
    (->MI-map m nil nil nil nil)))

(deftype MI-map [sorted-map start-test start-path end-test end-path]
  ILookup
  (valAt [this key]
    (if (in-range key start-test start-path end-test end-path)
      (get sorted-map key)
      nil))
  (valAt [this key not-found]
    (if (in-range key start-test start-path end-test end-path)
      (get sorted-map key not-found)
      not-found))
  Seqable
  (seq [this]
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
  IPersistentCollection
  (count [this]
    (count (seq this)))
  (cons [this o]
    (if (instance? java.util.Map$Entry o)
      (assoc this (key o) (val o))
      (if (instance? IPersistentVector o)
        (if (not= (count o) 2)
          (throw (IllegalArgumentException. "Vector arg to map conj must be a pair"))
          (assoc this (o 0) (o 1)))
        (let [s (seq o)]
          (reduce
            (fn [mi me] (assoc mi (key me) (val me)))
            this
            s)))))
  Associative
  (assoc [this path value]
    (if (in-range path start-test start-path end-test end-path)
      (->MI-map
        (assoc sorted-map path value)
        start-test start-path end-test end-path)
      this))
  Reversible
  (rseq [this]
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
  MI
  (mi-sub
    [this prefix]
    (let [[s-test s-path e-test e-path]
          (mi-munge prefix start-test start-path end-test end-path)]
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
          (mi-munge start-test start-path end-test end-path stest spath etest epath)]
      (if (and
            (= s-test start-test)
            (= e-test end-test)
            (= 0 (compare s-path start-path))
            (= 0 (compare e-path end-path)))
        this
        (->MI-map sorted-map s-test s-path e-test e-path)))))

(defmethod print-method MI-map [v ^java.io.Writer w]
  (.write w "#mapish/MI-map {")
  (reduce (fn [_ i]
            (.write w (str (key i) " " (val i) ", "))
            ) nil (seq v))
  (.write w "}")
  )
