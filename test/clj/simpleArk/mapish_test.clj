(ns simpleArk.mapish-test
  (:require [clojure.test :refer :all]
            [simpleArk.mapish :refer :all]))

(set! *warn-on-reflection* true)

(def a [0 1 2])
(def b [1 2 3])
(def c [0 1 2 3])
(def d [3 4])
(def e [2 11])
(def f [99 1])
(def x [0])
(def y [1])
(def z [])

(def sm0 (sorted-map-by
           vec-comp
           [:a] 1
           [:b] 2
           [:e] 10
           [:f] 11))
(def mi0 (->MI-map sm0 nil nil nil nil))
(def mi1 (->MI-map sm0 >= [:b] <= [:e]))
(def mi2 (->MI-map sm0 > [:a] < [:f]))
(def mama (mapish))
(def mea (mapish
           [:a] 0
           [:a 1] 1
           [:a 1 :x] 2
           [:a 1 :y] 3
           [:a 2] 4
           [:b] 5))

(deftest mapish-test
  (println (sorted-set-by vec-comp a b c d e f))
  (println sm0)
  (println mi0)
  (println (seq mi0))
  (println (seq mi1))
  (println (seq mi2))
  (println (seq (assoc mi0 [:c] 3)))
  (println (seq (assoc mi1 [:c] 3)))
  (println (seq (assoc mi0 [:a] 1)))
  (println (seq (assoc mi0 [:a] 11)))
  (println :rseq (rseq mi0))
  (println :rseq (rseq mi1))
  (println :rseq (rseq mi2))
  (println (get mi0 [:a]))
  (println (get mi1 [:a]))
  (println (get mi2 [:a]))
  (println (get mi0 [:b]))
  (println (get mi1 [:b]))
  (println (get mi2 [:b]))
  (println (get mi0 [:c]))
  (println (get mi0 [:e]))
  (println (get mi1 [:e]))
  (println (get mi2 [:e]))
  (println (get mi0 [:f]))
  (println (get mi1 [:f]))
  (println (get mi2 [:f]))
  (println (get mi0 [:a] 22))
  (println (get mi0 [:c] 22))
  (println (seq (mi-sub mi0 nil nil nil nil)))
  (println (seq (mi-sub mi1 nil nil nil nil)))
  (println (seq (mi-sub mi0 >= [:a] <= [:f])))
  (println (seq (mi-sub mi0 > [:a] < [:f])))
  (println (seq (mi-sub mi0 >= [:b] <= [:e])))
  (println (seq (mi-sub mi0 > [:b] < [:e])))
  (println (seq (mi-sub mi1 >= [:a] <= [:f])))
  (println (seq (mi-sub mi1 > [:a] < [:f])))
  (println (seq (mi-sub mi1 >= [:b] <= [:e])))
  (println (seq (mi-sub mi1 > [:b] < [:e])))
  (println (seq (mi-sub mi2 >= [:a] <= [:f])))
  (println (seq (mi-sub mi2 > [:a] < [:f])))
  (println (seq (mi-sub mi2 >= [:b] <= [:e])))
  (println (seq (mi-sub mi2 > [:b] < [:e])))
  (println :mama mama)
  (println :prstr (prn-str mama))
  (println :mama-sequence (sequence mama))
  (println :mama-seq (seq mama))
  (println (seq mea))
  (println (seq (mi-sub mea [:a])))
  (println (seq (into mama {[:z] 55}))))
