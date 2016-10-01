(ns simpleArk.mapish-test
  (:require [clojure.test :refer :all]
            [simpleArk.mapish :as mapish]))

(set! *warn-on-reflection* true)

(def sm0 (sorted-map :a 1 :b 2 :e 10 :f 11))
(def mi0 (mapish/->MI-map sm0 nil nil nil nil))

(deftest mapish
  (println sm0)
  (println mi0)
  (println (mapish/mi-seq mi0))
  (println (mapish/mi-rseq mi0))
  (println (mapish/mi-seq (mapish/mi-assoc mi0 :c 3)))
  (println (mapish/mi-seq (mapish/mi-dissoc mi0 :b)))
  )
