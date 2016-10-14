(ns simpleArk.vecish)

(set! *warn-on-reflection* true)

(defrecord vecish [v]
  java.lang.Comparable
  (compareTo [this o]
    (let [^vecish ov o]
      (if
        (= this ov)
        0
        (if
          (= (count v) (count (.v ov)))
          (compare v (.v ov))
          )))))
