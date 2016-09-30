(ns simpleArk.mapish)

(set! *warn-on-reflection* true)

(defprotocol MI
  (mi-get [this key] [key default])
  (mi-assoc [this key value])
  (mi-seq this)
  (mi-rseq this)
  (mi-sub [this start-test start-key] [this start-test start-key end-test end-key]))

(deftype MI-map [sorted-map start-test start-key end-test end-key]
  MI
  (mi-get [this key] (get sorted-map key))
  (mi-get [this key not-found] (get sorted-map key not-found))
  (mi-assoc [this key value] (assoc sorted-map key value))
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
  (mi-sub [this stest skey]
    (let [c (compare skey start-key)]
        (cond
          (< c 0)
          this
          (> c 0)
          (->MI-map sorted-map stest skey end-test end-key)
          (= stest start-test)
          this
          (= stest >)
          (->MI-map sorted-map stest skey end-test end-key)
          :else
          this
          )))
  )
