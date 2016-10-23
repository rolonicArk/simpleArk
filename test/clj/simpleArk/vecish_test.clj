(ns simpleArk.vecish-test
  (:require [clojure.test :refer :all]
            [simpleArk.vecish :refer :all]))

(set! *warn-on-reflection* true)

(def a (->Vecish [0 1 2]))
(def b (->Vecish [1 2 3]))
(def c (->Vecish [0 1 2 3]))
(def d (->Vecish [3 4]))
(def e (->Vecish [2 11]))
(def f (->Vecish [99 1]))
(def x (->Vecish [0]))
(def y (->Vecish [1]))
(def z (->Vecish []))

(deftest vecish
  (println a)
  (is (= [0 1 2] (:v a)))
  (is (= 0 (compare a a)))
  (is (= 1 (compare b c)))
  (is (= -1 (compare a d)))
  (is (= -1 (compare nil b)))
  (is (= 1 (compare b nil)))
  (is (= -1 (compare e f)))
  (println :vecish (map :v (sorted-set a b c d e f)))
  (println :vector (seq (sorted-set (:v a) (:v b) (:v c) (:v d) (:v e) (:v f))))
  (is (prefixed? a x))
  (is (not (prefixed? a y)))
  (is (prefixed? a z))
  (is (prefixed? c a))
  (is (not (prefixed? a c)))
  (println (compare (->Vecish [:a]) (->Vecish [:a nil])))
  (println (seq a) a)
  )

