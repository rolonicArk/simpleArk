(ns simpleArk.tlog.tlog1-test
  (:require [clojure.test :refer :all]
            [simpleArk.arkDb.ark-db :as ark-db]
            [simpleArk.tlog.tlog :as tlog]
            [simpleArk.tlog.tlog1 :as tlog1]
            [simpleArk.log.log0 :as log0]
            [simpleArk.pub.pub0 :as pub0]
            [simpleArk.sub.sub0 :as sub0]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(deftest tlog1
  (let [c ((comp
             (ark-db/builder)
             (tlog1/builder)
             (sub0/builder)
             (pub0/builder)
             (log0/builder))
            {})
        rsp-chan (async/chan 1)]
    (ark-db/update-ark-db! c "_")
    (tlog/add-tran! c nil nil 1 "a" "-" rsp-chan "x")
    (println (async/<!! rsp-chan) (ark-db/get-ark-record c))
    (tlog/add-tran! c nil nil 2 "b" "-" rsp-chan "y")
    (println (async/<!! rsp-chan) (ark-db/get-ark-record c))
    (tlog/add-tran! c nil nil 3 "c" "-" rsp-chan "z")
    (println (async/<!! rsp-chan) (ark-db/get-ark-record c))
    (println (tlog/tran-seq c))
    (println (tlog/tran-seq c 2))))
