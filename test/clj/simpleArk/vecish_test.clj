(ns simpleArk.vecish-test
  (:require [clojure.test :refer :all]
            [simpleArk.vecish :as vecish]))

(set! *warn-on-reflection* true)

(def a (vecish/->Vecish [0 1 2]))
(def b (vecish/->Vecish [1 2 3]))
(def c (vecish/->Vecish [0 1 2 3]))
(def d (vecish/->Vecish [3 4]))
(def e (vecish/->Vecish [2 11]))
(def f (vecish/->Vecish [99 1]))

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
  (println :vector (sorted-set (:v a) (:v b) (:v c) (:v d) (:v e) (:v f))))

