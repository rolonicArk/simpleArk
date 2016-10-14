(ns simpleArk.vecish)

(set! *warn-on-reflection* true)

(defrecord vecish [v]
  java.lang.Comparable
  (compareTo [this o]
    (if
      (= this o)
      0
      (let [^vecish ov o
            ovv (.v ov)
            c (count v)
            ovc (count ovv)
            mc (min c ovc)]
        (if
          (= c ovc)
          (compare v ovv)
          (loop [i 0]
                (if (>= i mc)
                  (compare c ovc)
                  (let [r (compare (v i) (ovv i))]
                    (if (not= r 0)
                      r
                      (recur (+ i 1)))))))))))
