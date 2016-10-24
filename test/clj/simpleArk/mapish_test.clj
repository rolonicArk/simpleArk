(ns simpleArk.mapish-test
  (:require [clojure.test :refer :all]
            [simpleArk.mapish :refer :all]
            [simpleArk.vecish :refer [->Vecish]]))

(set! *warn-on-reflection* true)

(def sm0 (sorted-map (->Vecish [:a]) 1
                     (->Vecish [:b]) 2
                     (->Vecish [:e]) 10
                     (->Vecish [:f]) 11))
(def mi0 (->MI-map sm0 nil nil nil nil))
(def mi1 (->MI-map sm0 >= (->Vecish [:b]) <= (->Vecish [:e])))
(def mi2 (->MI-map sm0 > (->Vecish [:a]) < (->Vecish [:f])))
(def mea (->MI-map
            (sorted-map
              (->Vecish [:a]) 0
              (->Vecish [:a 1]) 1
              (->Vecish [:a 1 :x]) 2
              (->Vecish [:a 1 :y]) 3
              (->Vecish [:a 2]) 4
              (->Vecish [:b]) 5
              )
            nil nil nil nil))

(deftest mapish
  (println sm0)
  (println mi0)
  (println (seq mi0))
  (println (seq mi1))
  (println (seq mi2))
  (println (seq (assoc mi0 (->Vecish [:c]) 3)))
  (println (seq (assoc mi1 (->Vecish [:c]) 3)))
  (println (seq (assoc mi0 (->Vecish [:a]) 1)))
  (println (seq (assoc mi0 (->Vecish [:a]) 11)))
  (println :rseq (rseq mi0))
  (println :rseq (rseq mi1))
  (println :rseq (rseq mi2))
  (println (get mi0 (->Vecish [:a])))
  (println (get mi1 (->Vecish [:a])))
  (println (get mi2 (->Vecish [:a])))
  (println (get mi0 (->Vecish [:b])))
  (println (get mi1 (->Vecish [:b])))
  (println (get mi2 (->Vecish [:b])))
  (println (get mi0 (->Vecish [:c])))
  (println (get mi0 (->Vecish [:e])))
  (println (get mi1 (->Vecish [:e])))
  (println (get mi2 (->Vecish [:e])))
  (println (get mi0 (->Vecish [:f])))
  (println (get mi1 (->Vecish [:f])))
  (println (get mi2 (->Vecish [:f])))
  (println (get mi0 (->Vecish [:a]) 22))
  (println (get mi0 (->Vecish [:c]) 22))
  (println (seq (mi-sub mi0 nil nil nil nil)))
  (println (seq (mi-sub mi1 nil nil nil nil)))
  (println (seq (mi-sub mi0 >= (->Vecish [:a]) <= (->Vecish [:f]))))
  (println (seq (mi-sub mi0 > (->Vecish [:a]) < (->Vecish [:f]))))
  (println (seq (mi-sub mi0 >= (->Vecish [:b]) <= (->Vecish [:e]))))
  (println (seq (mi-sub mi0 > (->Vecish [:b]) < (->Vecish [:e]))))
  (println (seq (mi-sub mi1 >= (->Vecish [:a]) <= (->Vecish [:f]))))
  (println (seq (mi-sub mi1 > (->Vecish [:a]) < (->Vecish [:f]))))
  (println (seq (mi-sub mi1 >= (->Vecish [:b]) <= (->Vecish [:e]))))
  (println (seq (mi-sub mi1 > (->Vecish [:b]) < (->Vecish [:e]))))
  (println (seq (mi-sub mi2 >= (->Vecish [:a]) <= (->Vecish [:f]))))
  (println (seq (mi-sub mi2 > (->Vecish [:a]) < (->Vecish [:f]))))
  (println (seq (mi-sub mi2 >= (->Vecish [:b]) <= (->Vecish [:e]))))
  (println (seq (mi-sub mi2 > (->Vecish [:b]) < (->Vecish [:e]))))
  (println (seq mea))
  (println (seq (mi-sub mea (->Vecish [:a]))))
  )
