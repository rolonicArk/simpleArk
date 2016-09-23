(ns simpleArk.ark-db0-test
  (:require [clojure.test :refer :all]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-value0 :as ark-value0]
            [simpleArk.log :as log]
            [simpleArk.logt :as logt]
            [simpleArk.uuid :as uuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.ark-db0 :as ark-db0]))

(set! *warn-on-reflection* true)

(defmethod ark-value/eval-transaction ::hello-world!
  [ark-value n s]
  (println "Hello," s)
  (let [je-uuid (ark-value/get-current-journal-entry-uuid ark-value)]
    (ark-value/update-property! ark-value je-uuid :classifier/headline "Just for fun!")))

(defn test0
  "tests that even work with impl0"
  [ark-db]

  (is (ark-value/classifier? :classifier/x))
  (is (not (ark-value/classifier? :descriptor/y)))
  (is (not (ark-value/classifier? :x)))
  (is (not (ark-value/classifier? ":classifier/x")))
  (is (ark-value/descriptor? :descriptor/x))
  (is (not (ark-value/descriptor? :classifier/y)))
  (is (not (ark-value/descriptor? :x)))
  (is (not (ark-value/descriptor? ":descriptor/x")))

  (ark-value/validate-property-keys {:classifier/x 1 :descriptor/y "fred"})
  (is (thrown? Exception (ark-value/validate-property-keys {1 2})))

  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)
  (def hello-je-uuid (ark-db/process-transaction! ark-db ::hello-world! "Fred"))
  (is (= :transaction ((log/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> make-bob")
  (println)
  (def bob-uuid (uuid/random-uuid ark-db))
  (def make-bob-je-uuid (ark-db/process-transaction! ark-db :ark/update-rolon-transaction!
                                                     (prn-str [bob-uuid
                                                               {:classifier/headline "make bob"}
                                                               {:descriptor/age 8 :classifier/name "Bob"}])))
  (is (= :transaction ((log/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> 4 updates to bob")
  (println)
  (ark-db/process-transaction! ark-db :ark/update-rolon-transaction!
                               (prn-str [bob-uuid
                                         {:classifier/headline "bob update 1"}
                                         {:classifier/headline "kissing is gross!"}]))
  (is (= :transaction ((log/get-msg ark-db) 1)))
  (ark-db/process-transaction! ark-db :ark/update-rolon-transaction!
                               (prn-str [bob-uuid
                                         {:classifier/headline "bob update 2"}
                                         {:descriptor/age 9}]))
  (is (= :transaction ((log/get-msg ark-db) 1)))
  (ark-db/process-transaction! ark-db :ark/update-rolon-transaction!
                               (prn-str [bob-uuid
                                         {:classifier/headline "bob update 3"}
                                         {:classifier/headline "who likes girls?"}]))
  (is (= :transaction ((log/get-msg ark-db) 1)))
  (ark-db/process-transaction! ark-db :ark/update-rolon-transaction!
                               (prn-str [bob-uuid
                                         {:classifier/headline "bob update 4"}
                                         {:classifier/headline "when do I get my own mobile!"}]))
  (is (= :transaction ((log/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> make-sam")
  (println)
  (def sam-uuid (uuid/random-uuid ark-db))
  (def make-sam-je-uuid (ark-db/process-transaction! ark-db :ark/update-rolon-transaction!
                                                     (prn-str [sam-uuid
                                                               {:classifier/headline "make sam"}
                                                               {:descriptor/age 10
                                                                :classifier/name "Sam"
                                                                :classifier/headline "I hate green eggs and ham!"}])))
  (is (= :transaction ((log/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> destroy-bob")
  (println)
  (def destroy-bob-je-uuid (ark-db/process-transaction! ark-db :ark/destroy-rolon-transaction!
                                                        (prn-str [bob-uuid
                                                                  {:classifier/headline "destroy bob"}])))
  (is (= :transaction ((log/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>> select time: make-bob-je-uuid")
  (println)
  (let [ark-value (ark-db/get-ark-value ark-db)
        ark-value (ark-value/select-time ark-value make-bob-je-uuid)]
    (println :bob-properties (ark-value/get-current-property-values ark-value bob-uuid))
    (println :lookup-bob (ark-value/name-lookup ark-value "Bob")))

  (println)
  (println ">>>>>>>>>>>> journal entry headlines")
  (println)
  (let [ark-value (ark-db/get-ark-value ark-db)]
    (first (keep (fn [x] (println (ark-value/get-current-property-value ark-value (key x) :classifier/headline)))
                 (ark-value/get-journal-entries ark-value))))

  (println)
  (println ">>>>>>>>>>>> all the latest headlines")
  (println)
  (let [ark-value (ark-db/get-ark-value ark-db)
        headline-index-uuid (ark-value/get-index-uuid ark-value "headline")
        current-rolon-value (ark-value/get-current-property-values ark-value headline-index-uuid)
        descriptor-index (:descriptor/index current-rolon-value)]
    (first (keep (fn [x]
                   (if (first (val x))
                     (println (key x))))
                 descriptor-index)))

  (println)
  (println ">>>>>>>>>>>> bob's headlines over time")
  (println)
  (let [ark-value (ark-db/get-ark-value ark-db)]
    (first (keep #(println (ark-value/get-property-value-at ark-value bob-uuid :classifier/headline %)
                           "-"
                           (ark-value/get-current-property-value ark-value % :classifier/headline))
                 (ark-value/rolon-property-current-je-uuids ark-value bob-uuid :classifier/headline)))))

(deftest arks
  (println "impl0 tests")
  (def ark-db ((comp
                 (ark-db/builder)
                 (ark-db0/builder)
                 (ark-value0/builder)
                 (uuidi/builder)
                 (logt/builder))
                {}))
  (ark-db/open-ark! ark-db)
  (test0 ark-db))
