(ns simpleArk.arkDb.ark-db0-test
  (:require [clojure.test :refer :all]
            [simpleArk.arkValue.ark-value :as ark-value]
            [simpleArk.arkValue.ark-value0 :as ark-value0]
            [simpleArk.log.log :as log]
            [simpleArk.log.logt :as logt]
            [simpleArk.uuid :as suuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.arkDb.ark-db :as ark-db]
            [simpleArk.arkDb.ark-db0 :as ark-db0]
            [simpleArk.closer :as closer]
            [simpleArk.mapish :as mapish]
            [simpleArk.reader :as reader]
            [simpleArk.miMap :as miMap]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.actions]
            [simpleArk.uuid :as uuid]))

(set! *warn-on-reflection* true)

(defmethod ark-value/eval-transaction ::hello-world!
  [ark-record ark-db n s]
  (println "Hello," s)
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)]
    (ark-value/update-property ark-record ark-db je-uuid [:index/headline] "Just for fun!")))

(defn test0
  "tests that even work with impl0"
  [ark-db]

  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)
  (def hello-je-uuid (ark-db/process-transaction! ark-db ::hello-world! "Fred"))
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

  (do
    (println)
    (println ">>>>>>>>>>>> make-bob")
    (println)
    (def bob-uuid (suuid/random-uuid ark-db))
    (def make-bob-je-uuid
      (ark-db/process-transaction!
        ark-db
        :ark/update-rolon-transaction!
        (prn-str
          [bob-uuid
           {[:index/headline] "make bob"}
           {[:content/age]             8
            [:index/name]              "Bob"
            [:content/brothers "John"] true
            [:content/brothers "Jeff"] true}]))
      )
    (is (= :transaction ((log/get-msg ark-db) 1)))
    (let [ark-record (ark-db/get-ark-record ark-db)]
      (println :rel/modified (arkRecord/get-related-uuids ark-record make-bob-je-uuid :rel/modified))
      (println :inv-rel/modified (arkRecord/get-related-uuids ark-record make-bob-je-uuid :inv-rel/modified))
      (println :bob-properties (arkRecord/get-property-values ark-record bob-uuid))
      (println :lookup-bob (arkRecord/name-lookup ark-record "Bob"))
      (println :brothers
               (mapish/mi-sub
                 (arkRecord/get-property-values ark-record bob-uuid)
                 [:content/brothers])))

    (println)
    (println ">>>>>>>>>>>> 4 updates to bob")
    (println)
    (ark-db/process-transaction!
      ark-db
      :ark/update-rolon-transaction!
      (prn-str
        [bob-uuid
         {[:index/headline] "bob update 1"}
         {[:index/headline] "kissing is gross!"}]))
    (is (= :transaction ((log/get-msg ark-db) 1)))
    (ark-db/process-transaction!
      ark-db
      :ark/update-rolon-transaction!
      (prn-str
        [bob-uuid
         {[:index/headline] "bob update 2"}
         {[:content/age] 9}]))
    (is (= :transaction ((log/get-msg ark-db) 1)))
    (ark-db/process-transaction!
      ark-db
      :ark/update-rolon-transaction!
      (prn-str
        [bob-uuid
         {[:index/headline] "bob update 3"}
         {[:index/headline] "who likes girls?"}]))
    (is (= :transaction ((log/get-msg ark-db) 1)))
    (ark-db/process-transaction!
      ark-db
      :ark/update-rolon-transaction!
      (prn-str
        [bob-uuid
         {[:index/headline] "bob update 4"}
         {[:index/headline] "when do I get my own mobile!"}]))
    (is (= :transaction ((log/get-msg ark-db) 1)))

    (println)
    (println ">>>>>>>>>>>> make-sam")
    (println)
    (def sam-uuid (suuid/random-uuid ark-db))
    (def make-sam-je-uuid
      (ark-db/process-transaction!
        ark-db
        :ark/update-rolon-transaction!
        (prn-str
          [sam-uuid
           {[:index/headline] "make sam"}
           {[:content/age]    10
            [:index/name]     "Sam"
            [:index/headline] "I hate green eggs and ham!"}])))
    (is (= :transaction ((log/get-msg ark-db) 1)))

    (println)
    (println ">>>>>>>>>>>> destroy-bob")
    (println)
    (def destroy-bob-je-uuid
      (ark-db/process-transaction!
        ark-db
        :ark/destroy-rolon-transaction!
        (prn-str
          [bob-uuid
           {[:index/headline] "destroy bob"}])))
    (is (= :transaction ((log/get-msg ark-db) 1)))
    (let [ark-record (ark-db/get-ark-record ark-db)]
      (println :bob-properties (arkRecord/get-property-values ark-record bob-uuid))
      (println :lookup-bob (arkRecord/name-lookup ark-record "Bob")))

    (println)
    (println ">>>>>>>>>> select time: make-bob-je-uuid")
    (println)
    (let [ark-record (ark-db/get-ark-record ark-db)
          _ (println "total je count:" (count (arkRecord/get-journal-entries ark-record)))
          ark-record (arkRecord/select-time ark-record make-bob-je-uuid)]
      (println "selected je count:" (count (arkRecord/get-journal-entries ark-record)))
      (println :lookup-bob (arkRecord/name-lookup ark-record "Bob"))
      (println :bob-properties (arkRecord/get-property-values ark-record bob-uuid))
      )

    (println)
    (println ">>>>>>>>>>>> journal entry headlines")
    (println)
    (let [ark-record (ark-db/get-ark-record ark-db)]
      (first (keep (fn
                     [x]
                     (println (arkRecord/get-property-value ark-record (rolonRecord/get-rolon-uuid (val x)) [:index/headline])))
                   (seq (arkRecord/get-journal-entries ark-record)))))

    (println)
    (println ">>>>>>>>>>>> all the latest headlines")
    (println)
    (let [ark-record (ark-db/get-ark-record ark-db)
          headline-index-uuid (arkRecord/get-index-uuid ark-record "headline")
          content-index (arkRecord/get-content-index
                          ark-record
                          headline-index-uuid)]
      (doall (map #(println (first %)) content-index)))

    (println)
    (println ">>>>>>>>>>>> bob's headlines over time")
    (println)
    (let [ark-record (ark-db/get-ark-record ark-db)]
      (first (keep (fn [x]
                     (println (val x)
                              "-"
                              (arkRecord/get-property-value ark-record
                                                            (arkRecord/get-journal-entry-uuid ark-record (first (key x)))
                                                            [:index/headline])))
                   (rseq (arkRecord/get-changes-by-property ark-record
                                                            bob-uuid
                                                            [:index/headline])))))
    ))

(deftest arks
  (println "impl0 tests")
  (def ark-db ((comp
                 (ark-db/builder)
                 (ark-db0/builder)
                 (ark-value0/builder)
                 (uuidi/builder)
                 (closer/builder)
                 (logt/builder)
                 (reader/builder))
                {}))
  (uuid/register ark-db)
  (miMap/register ark-db)
  (arkRecord/register ark-db)
  (rolonRecord/register ark-db)

  (ark-db/open-ark! ark-db)
  (try
    (test0 ark-db)
    (finally
      (closer/close-all ark-db))))
