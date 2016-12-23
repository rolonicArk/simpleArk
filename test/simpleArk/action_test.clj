(ns simpleArk.action-test
  (:require [clojure.test :refer :all]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.log :as log]
            [simpleArk.logt :as logt]
            [simpleArk.uuid :as suuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.ark-db0 :as ark-db0]
            [simpleArk.closer :as closer]
            [simpleArk.mapish :as mapish]
            [simpleArk.reader :as reader]
            [simpleArk.miMap :as miMap]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.actions]
            [simpleArk.builder :as builder]))

(set! *warn-on-reflection* true)

(defn hello-transaction
  [ark-db name]
  (builder/transaction!
    ark-db {}
    (-> []
        (builder/build-println
          (str "Hello " name "!"))
        (builder/build-je-property
          [:index/headline] "Just for fun!"))))

(defn test0
  [ark-db]

  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)

  (def hello-je-uuid
      (hello-transaction ark-db "Fred"))

  (println)
  (println ">>>>>>>>>>>> transaction names")
  (println)
  (let [ark-record (ark-db/get-ark-record ark-db)
        index-uuid (arkRecord/get-index-uuid ark-record "transaction-name")
        content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    ;(mapish/debug [:content content-index])
    (doall (map #(println (first %)) content-index)))

  (println)
  (println ">>>>>>>>>>>> all the latest headlines")
  (println)
  (let [ark-record (ark-db/get-ark-record ark-db)
        headline-index-uuid (arkRecord/get-index-uuid ark-record "headline")
        content-index (arkRecord/get-content-index
                        ark-record
                        headline-index-uuid)]
    (doall (map #(println (first %)) content-index)))
  )

(deftest arks
  (println "action tests")
  (def ark-db ((comp
                 (ark-db/builder)
                 (ark-db0/builder)
                 (ark-value0/builder)
                 (uuidi/builder)
                 (closer/builder)
                 (logt/builder)
                 (reader/builder))
                {}))
  (miMap/register ark-db)
  (arkRecord/register ark-db)
  (rolonRecord/register ark-db)

  (ark-db/open-ark! ark-db)
  (try
    (test0 ark-db)
    (finally
      (closer/close-all ark-db))))
