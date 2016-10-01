(ns simpleArk.mapish-test
  (:require [clojure.test :refer :all]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(def sm0 (sorted-map :a 1 :b 2 :e 10 :f 11))
(def mi0 (mapish/->MI-map sm0 nil nil nil nil))
(def mi1 (mapish/->MI-map sm0 >= :b <= :e))
(def mi2 (mapish/->MI-map sm0 > :a < :f))

(deftest mapish
  (println sm0)
  (println mi0)
  (println (mapish/mi-seq mi0))
  (println (mapish/mi-seq mi1))
  (println (mapish/mi-seq mi2))
  (println (mapish/mi-rseq mi0))
  (println (mapish/mi-rseq mi1))
  (println (mapish/mi-rseq mi2))
  (println (mapish/mi-seq (mapish/mi-assoc mi0 :c 3)))
  ;(println (mapish/mi-seq (mapish/mi-assoc mi1 :c 3)))
  (println (mapish/mi-seq (mapish/mi-assoc mi0 :a 1)))
  (println (mapish/mi-seq (mapish/mi-assoc mi0 :a 11)))
  (println (mapish/mi-seq (mapish/mi-dissoc mi0 :b)))
  (println (mapish/mi-seq (mapish/mi-dissoc mi0 :c)))
  (println (mapish/mi-get mi0 :a))
  (println (mapish/mi-get mi0 :a 22))
  (println (mapish/mi-get mi0 :c))
  (println (mapish/mi-get mi0 :c 22))
  )
