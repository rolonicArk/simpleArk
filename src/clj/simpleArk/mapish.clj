(ns simpleArk.mapish)

(set! *warn-on-reflection* true)

(defprotocol MI
  (mi-get [this key] [this key default])
  (mi-seq [this])
  (mi-rseq [this])
  (mi-sub [this prefix start-test start-key end-test end-key]))

(defprotocol MIU
  (mi-assoc [this key value]))

(defn in-range [key stest skey etest ekey]
  (let [sc (compare key skey)
        ec (compare key ekey)]
    (and
      (cond
        (nil? skey)
        true
        (> 0 sc)
        false
        (< 0 sc)
        true
        :else
        (= stest >=))
      (cond
        (nil? ekey)
        true
        (< 0 ec)
        false
        (> 0 ec)
        true
        :else
        (= etest <=)))))

(declare ->MI-map)

(deftype MI-map [sorted-map start-test start-key end-test end-key]
  MI
  (mi-get [this key]
    (if (in-range key start-test start-key end-test end-key)
      (get sorted-map key)
      nil))
  (mi-get [this key not-found]
    (if (in-range key start-test start-key end-test end-key)
      (get sorted-map key not-found)
      not-found))
  (mi-seq [this]
    (cond
      (empty? sorted-map)
      nil
      (or start-key end-key)
      (let [
            start-test (if start-key
                         start-test
                         >=)
            start-key (if start-key
                        start-key
                        (key (first sorted-map)))
            end-test (if end-key
                       end-test
                       <=)
            end-key (if end-key
                      end-key
                      (key (last sorted-map)))]
        (subseq sorted-map start-test start-key end-test end-key))
      :else
      (seq sorted-map)))
  (mi-rseq [this]
    (cond
      (empty? sorted-map)
      nil
      (or start-key end-key)
      (let [
            start-test (if start-key
                         start-test
                         >=)
            start-key (if start-key
                        start-key
                        (key (first sorted-map)))
            end-test (if end-key
                       end-test
                       <=)
            end-key (if end-key
                      end-key
                      (key (last sorted-map)))]
        (rsubseq sorted-map start-test start-key end-test end-key))
      :else
      (rseq sorted-map)))
  (mi-sub [this prefix stest skey etest ekey]
    (let [sc (compare skey start-key)
          s-test (cond
                   (nil? skey)
                   start-test
                   (nil? start-key)
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
          s-key (cond
                  (nil? skey)
                  start-key
                  (nil? start-key)
                  skey
                  (< sc 0)
                  start-key
                  (> sc 0)
                  skey
                  (= stest start-test)
                  start-key
                  (= stest >)
                  skey
                  :else
                  start-key)
          ec (compare ekey end-key)
          e-test (cond
                   (nil? ekey)
                   end-test
                   (nil? end-key)
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
          e-key (cond
                  (nil? ekey)
                  end-key
                  (nil? end-key)
                  ekey
                  (> ec 0)
                  end-key
                  (< ec 0)
                  ekey
                  (= etest end-test)
                  end-key
                  (= etest <)
                  ekey
                  :else
                  end-key)]
      (if (and
            (= s-test start-test)
            (= e-test end-test)
            (= 0 (compare s-key start-key))
            (= 0 (compare e-key end-key)))
        this
        (->MI-map sorted-map s-test s-key e-test e-key))))

  MIU
  (mi-assoc [this key value]
    (if (in-range key start-test start-key end-test end-key)
      (->MI-map
        (assoc sorted-map key value)
        start-test start-key end-test end-key)
      this)))
