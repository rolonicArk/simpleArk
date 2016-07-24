(ns simpleArk.tlog0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :as ark]
            [simpleArk.tlog0 :as tlog0]))

(set! *warn-on-reflection* true)

(deftest tlog0
  (let [c ((comp
            (tlog0/builder))
           {})]
    (ark/add-tran! c 1 "a" "-")
    (ark/add-tran! c 2 "b" "-")
    (ark/add-tran! c 3 "c" "-")
    (println (ark/tran-seq c))
    (println (ark/tran-seq c 2))
    ))
