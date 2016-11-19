(ns simpleArk.tlog0-test
  (:require [clojure.test :refer :all]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.tlog :as tlog]
            [simpleArk.tlog0 :as tlog0]
            [simpleArk.log0 :as log0]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(deftest tlog0
  (let [c ((comp
             (ark-db/builder)
             (tlog0/builder)
             (log0/builder))
            {})
        rsp-chan (async/chan 1)]
    (ark-db/init-ark-db! c "_")
    (tlog/add-tran! c 1 "a" "-" rsp-chan "x")
    (println (async/<!! rsp-chan) (ark-db/get-ark-record c))
    (tlog/add-tran! c 2 "b" "-" rsp-chan "y")
    (println (async/<!! rsp-chan) (ark-db/get-ark-record c))
    (tlog/add-tran! c 3 "c" "-" rsp-chan "z")
    (println (async/<!! rsp-chan) (ark-db/get-ark-record c))
    (println (tlog/tran-seq c))
    (println (tlog/tran-seq c 2))))
