(ns simpleArk.tlog1-test
  (:require [clojure.test :refer :all]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.tlog :as tlog]
            [simpleArk.tlog1 :as tlog1]
            [simpleArk.log0 :as log0]
            [simpleArk.pub0 :as pub0]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(deftest tlog1
  (let [c ((comp
             (ark-db/builder)
             (tlog1/builder)
             (pub0/builder)
             (log0/builder))
            {})
        rsp-chan (async/chan 1)]
    (ark-db/init-ark! c "_")
    (tlog/add-tran! c 1 "a" "-" rsp-chan "x")
    (println (async/<!! rsp-chan) (ark-db/get-ark-value c))
    (tlog/add-tran! c 2 "b" "-" rsp-chan "y")
    (println (async/<!! rsp-chan) (ark-db/get-ark-value c))
    (tlog/add-tran! c 3 "c" "-" rsp-chan "z")
    (println (async/<!! rsp-chan) (ark-db/get-ark-value c))
    (println (tlog/tran-seq c))
    (println (tlog/tran-seq c 2))))
