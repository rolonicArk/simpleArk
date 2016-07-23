(ns simpleArk.core-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]
            [simpleArk.logt :as logt]
            [simpleArk.uuid :as uuid]
            [simpleArk.uuidi :as uuidi]
            [simpleArk.reg0 :as reg0]
            [simpleArk.ark-db0 :as ark-db0]))

(set! *warn-on-reflection* true)

(defn hello-world!
  "simple transaction test"
  [s]
  (println "Hello," s)
  (let [je-uuid (get-current-journal-entry-uuid)]
    (update-property! je-uuid :classifier/headline "Just for fun!")))

(defn test0
  "tests that even work with impl0"
  [ark-db]

  (is (classifier? :classifier/x))
  (is (not (classifier? :descriptor/y)))
  (is (not (classifier? :x)))
  (is (not (classifier? ":classifier/x")))
  (is (descriptor? :descriptor/x))
  (is (not (descriptor? :classifier/y)))
  (is (not (descriptor? :x)))
  (is (not (descriptor? ":descriptor/x")))

  (validate-property-keys {:classifier/x 1 :descriptor/y "fred"})
  (is (thrown? Exception (validate-property-keys {1 2})))

  (register-transaction! ark-db ::hello-world! hello-world!)
  (register-transaction! ark-db ::update-rolon-transaction! update-rolon-transaction!)
  (register-transaction! ark-db ::destroy-rolon-transaction! destroy-rolon-transaction!)

  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)
  (def hello-je-uuid (process-transaction! ark-db ::hello-world! "Fred"))
  (is (= :transaction ((logt/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> make-bob")
  (println)
  (def bob-uuid (uuid/random-uuid ark-db))
  (def make-bob-je-uuid (process-transaction! ark-db ::update-rolon-transaction!
                                              (prn-str [bob-uuid
                                                        {:classifier/headline "make bob"}
                                                        {:descriptor/age 8 :classifier/name "Bob"}])))
  (is (= :transaction ((logt/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> 4 updates to bob")
  (println)
  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "bob update 1"}
                                  {:classifier/headline "kissing is gross!"}]))
  (is (= :transaction ((logt/get-msg ark-db) 1)))
  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "bob update 2"}
                                  {:descriptor/age 9}]))
  (is (= :transaction ((logt/get-msg ark-db) 1)))
  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "bob update 3"}
                                  {:classifier/headline "who likes girls?"}]))
  (is (= :transaction ((logt/get-msg ark-db) 1)))
  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "bob update 4"}
                                  {:classifier/headline "when do I get my own mobile!"}]))
  (is (= :transaction ((logt/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> make-sam")
  (println)
  (def sam-uuid (uuid/random-uuid ark-db))
  (def make-sam-je-uuid (process-transaction! ark-db ::update-rolon-transaction!
                                              (prn-str [sam-uuid
                                                        {:classifier/headline "make sam"}
                                                        {:descriptor/age 10
                                                         :classifier/name "Sam"
                                                         :classifier/headline "I hate green eggs and ham!"}])))
  (is (= :transaction ((logt/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>>>> destroy-bob")
  (println)
  (def destroy-bob-je-uuid (process-transaction! ark-db ::destroy-rolon-transaction!
                                                 (prn-str [bob-uuid
                                                {:classifier/headline "destroy bob"}])))
  (is (= :transaction ((logt/get-msg ark-db) 1)))

  (println)
  (println ">>>>>>>>>> select time: make-bob-je-uuid")
  (println)
  (bind-ark ark-db
            (select-time! make-bob-je-uuid)
            (println :bob-properties (get-property-values-at bob-uuid))
            (println :lookup-bob (name-lookup "Bob")))

  (println)
  (println ">>>>>>>>>>>> journal entry headlines")
  (println)
  (bind-ark ark-db
            (first (keep (fn [x] (println (get-property-value-at (key x) :classifier/headline)))
                         (get-journal-entries))))

(println)
(println ">>>>>>>>>>>> all the latest headlines")
(println)
(bind-ark ark-db
          (let [headline-index-uuid (get-index-uuid "headline")
                current-rolon-value (get-property-values-at headline-index-uuid)
                descriptor-index (:descriptor/index current-rolon-value)]
            (first (keep (fn [x]
                           (if (first (val x))
                             (println (key x))))
                         descriptor-index))
            ))

  (println)
  (println ">>>>>>>>>>>> bob's headlines over time")
  (println)
  (bind-ark ark-db
            (first (keep #(println (get-property-value-at bob-uuid :classifier/headline %)
                                   "-"
                                   (get-property-value-at % :classifier/headline))
                          (je-uuids-for-rolon-property bob-uuid :classifier/headline))))
  )

(deftest arks
  (println "impl0 tests")
  (def ark-db ((comp
                 (ark-db0/builder)
                 (impl0/builder)
                 (reg0/builder)
                 (uuidi/builder)
                 (logt/builder))
                {}))
  (open-ark ark-db)
  (test0 ark-db))
