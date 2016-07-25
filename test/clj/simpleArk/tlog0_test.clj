(ns simpleArk.tlog0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :as ark]
            [simpleArk.tlog0 :as tlog0]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(deftest tlog0
  (let [c ((comp
            (tlog0/builder))
           {})
        rsp-chan (async/chan 1)]
    (ark/add-tran! c 1 "a" "-" rsp-chan)
    (println (async/<!! rsp-chan))
    (ark/add-tran! c 2 "b" "-" rsp-chan)
    (println (async/<!! rsp-chan))
    (ark/add-tran! c 3 "c" "-" rsp-chan)
    (println (async/<!! rsp-chan))
    (println (ark/tran-seq c))
    (println (ark/tran-seq c 2))
    ))
