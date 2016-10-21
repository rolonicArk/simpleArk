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
  (println (mi-seq mi0))
  (println (mi-seq mi1))
  (println (mi-seq mi2))
  (println (mi-seq (mi-assoc mi0 (->Vecish [:c]) 3)))
  (println (mi-seq (mi-assoc mi1 (->Vecish [:c]) 3)))
  (println (mi-seq (mi-assoc mi0 (->Vecish [:a]) 1)))
  (println (mi-seq (mi-assoc mi0 (->Vecish [:a]) 11)))
  (println (mi-rseq mi0))
  (println (mi-rseq mi1))
  (println (mi-rseq mi2))
  (println (mi-get mi0 (->Vecish [:a])))
  (println (mi-get mi1 (->Vecish [:a])))
  (println (mi-get mi2 (->Vecish [:a])))
  (println (mi-get mi0 (->Vecish [:b])))
  (println (mi-get mi1 (->Vecish [:b])))
  (println (mi-get mi2 (->Vecish [:b])))
  (println (mi-get mi0 (->Vecish [:c])))
  (println (mi-get mi0 (->Vecish [:e])))
  (println (mi-get mi1 (->Vecish [:e])))
  (println (mi-get mi2 (->Vecish [:e])))
  (println (mi-get mi0 (->Vecish [:f])))
  (println (mi-get mi1 (->Vecish [:f])))
  (println (mi-get mi2 (->Vecish [:f])))
  (println (mi-get mi0 (->Vecish [:a]) 22))
  (println (mi-get mi0 (->Vecish [:c]) 22))
  (println (mi-seq (mi-sub mi0 nil nil nil nil)))
  (println (mi-seq (mi-sub mi1 nil nil nil nil)))
  (println (mi-seq (mi-sub mi0 >= (->Vecish [:a]) <= (->Vecish [:f]))))
  (println (mi-seq (mi-sub mi0 > (->Vecish [:a]) < (->Vecish [:f]))))
  (println (mi-seq (mi-sub mi0 >= (->Vecish [:b]) <= (->Vecish [:e]))))
  (println (mi-seq (mi-sub mi0 > (->Vecish [:b]) < (->Vecish [:e]))))
  (println (mi-seq (mi-sub mi1 >= (->Vecish [:a]) <= (->Vecish [:f]))))
  (println (mi-seq (mi-sub mi1 > (->Vecish [:a]) < (->Vecish [:f]))))
  (println (mi-seq (mi-sub mi1 >= (->Vecish [:b]) <= (->Vecish [:e]))))
  (println (mi-seq (mi-sub mi1 > (->Vecish [:b]) < (->Vecish [:e]))))
  (println (mi-seq (mi-sub mi2 >= (->Vecish [:a]) <= (->Vecish [:f]))))
  (println (mi-seq (mi-sub mi2 > (->Vecish [:a]) < (->Vecish [:f]))))
  (println (mi-seq (mi-sub mi2 >= (->Vecish [:b]) <= (->Vecish [:e]))))
  (println (mi-seq (mi-sub mi2 > (->Vecish [:b]) < (->Vecish [:e]))))
  (println (mi-seq mea))
  (println (mi-seq (mi-sub mea (->Vecish [:a]))))
  )
