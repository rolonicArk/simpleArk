(ns simpleArk.vecish)

(set! *warn-on-reflection* true)

(defrecord Vecish [v]
  java.lang.Comparable
  (compareTo [this o]
    (if
      (= this o)
      0
      (let [^Vecish ov o
            ovv (.v ov)
            c (count v)
            ovc (count ovv)
            mc (min c ovc)]
        (loop [i 0]
          (if (>= i mc)
            (compare c ovc)
            (let [vi (v i)
                  ovvi (ovv i)
                  r (compare vi ovvi)]
              (if (not= r 0)
                (if (nil? vi)
                  1
                  (if (nil? ovvi)
                    -1
                    r))
                (recur (+ i 1))))))))))

(defn prefixed? [path prefix]
  (if (nil? prefix)
    true
    (if (not (instance? simpleArk.vecish.Vecish path))
      false
      (let [path-vec (:v path)
            path-count (count path-vec)
            prefix-vec (:v prefix)
            prefix-count (count prefix-vec)]
        (if (< path-count prefix-count)
          false
          (loop [i 0]
            (if (>= i prefix-count)
              true
              (if (not= (path-vec i) (prefix-vec i))
                false
                (recur (+ i 1))))))))))
