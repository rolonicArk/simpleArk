(ns simpleArk.mapish)

#?(:clj
   (set! *warn-on-reflection* true))

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
        #?(:clj (throw (Exception. (str property-path " has too many elements for a classifier")))
           :cljs (throw (str property-path " has too many elements for a classifier"))))
      (if (not (content? kw))
        #?(:clj (throw (Exception. (str property-path " is neither a classifier nor a descriptor")))
           :cljs (throw (str property-path " is neither a classifier nor a descriptor")))))))

(defn validate-properties
  [properties]
  (reduce (fn [_ e]
            (let [path (key e)
                  kw (first path)
                  v (val e)]
              (validate-property-path path)
              (if (and (or (bi-rel? kw) (rel? kw) (inv-rel? kw))
                       (not (uuid? v)))
                #?(:clj (throw (Exception. (str path " is not assigned a UUID")))
                   :cljs (throw (str path " is not assigned a UUID"))))))
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
