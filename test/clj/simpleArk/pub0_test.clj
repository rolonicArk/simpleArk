(ns simpleArk.pub0-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :as ark]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.pub0 :as pub0]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(deftest pub0
  (let [c ((comp
             (pub0/builder))
            {})
        rsp-chan (async/chan 10)]
    (ark-db/init-ark! c "_")
    (ark/publish c "x" [[rsp-chan 1]])
    (println (async/<!! rsp-chan) (ark-db/get-ark c))
    (ark/publish c "z" [[rsp-chan 2] [rsp-chan 3]])
    (println (async/<!! rsp-chan) (ark-db/get-ark c))
    (println (async/<!! rsp-chan) (ark-db/get-ark c))
    ))
