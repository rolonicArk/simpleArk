(ns simpleArk.tlog0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :as ark]
            [simpleArk.tlog0 :as tlog0]
            [simpleArk.log0 :as log0]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(deftest tlog0
  (let [c ((comp
            (tlog0/builder)
            (log0/builder))
           {})
        rsp-chan (async/chan 1)]
    (ark/init-ark! c "_")
    (ark/add-tran! c 1 "a" "-" rsp-chan "x")
    (println (async/<!! rsp-chan) (ark/get-ark c))
    (ark/add-tran! c 2 "b" "-" rsp-chan "y")
    (println (async/<!! rsp-chan) (ark/get-ark c))
    (ark/add-tran! c 3 "c" "-" rsp-chan "z")
    (println (async/<!! rsp-chan) (ark/get-ark c))
    (println (ark/tran-seq c))
    (println (ark/tran-seq c 2))
    ))
