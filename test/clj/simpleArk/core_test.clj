(ns simpleArk.core-test
  (:require [clojure.test :refer :all]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]))

(deftest basic
  "basic tests"

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

  (def je-uuid0 (journal-entry-uuid))
  (def random-uuid0 (random-uuid))
  (def index-uuid0 (index-uuid :classifier/z))
  (is (thrown? Exception (index-uuid 1)))
  (is (thrown? Exception (index-uuid :descriptor/y)))

  (is (journal-entry-uuid? je-uuid0))
  (is (not (journal-entry-uuid? 42)))
  (is (not (journal-entry-uuid? random-uuid0)))
  (is (not (journal-entry-uuid? index-uuid0)))

  (is (random-uuid? random-uuid0))
  (is (not (random-uuid? 42)))
  (is (not (random-uuid? je-uuid0)))
  (is (not (random-uuid? index-uuid0)))

  (is (index-uuid? index-uuid0))
  (is (not (index-uuid? 42)))
  (is (not (index-uuid? je-uuid0)))
  (is (not (index-uuid? random-uuid0)))
  )

(defn hello-world
  "simple transaction test"
  [ark s]
  (println "Hello," s)
  (let [je-uuid (get-current-journal-entry-uuid ark)
        ark (update-property ark je-uuid :classifier/headline "Just for fun!")]
    ark))

(defn test0
  "tests that even work with impl0"
  [ark-db]
  (register-transaction! ark-db ::make-rolon-transaction make-rolon-transaction)
  (register-transaction! ark-db ::destroy-rolon-transaction destroy-rolon-transaction)

  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)
  (register-transaction! ark-db ::hello-world hello-world)
  (let [je-uuid (process-transaction! ark-db ::hello-world "Fred")
        ark (get-ark ark-db)
        je-properties (get-current-property-values ark je-uuid)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties)
    )

  (println)
  (println ">>>>>>>>>>>> make-bob")
  (println)
  (let [je-uuid (process-transaction! ark-db ::make-rolon-transaction
                                      (prn-str {:descriptor/age 8
                                                :classifier/name "Bob"}))
        ark (get-ark ark-db)
        je-properties (get-current-property-values ark je-uuid)
        bob-uuid (name-lookup ark "Bob")
        bob-properties (get-current-property-values ark bob-uuid)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties)
    (println :bob-uuid bob-uuid)
    (println :bob-properties bob-properties)
    )

  (println)
  (println ">>>>>>>>>>>> make-sam")
  (println)
  (let [je-uuid (process-transaction! ark-db ::make-rolon-transaction
                                      (prn-str {:descriptor/age 10
                                                :classifier/name "Sam"
                                                :classifier/headline "I hate green eggs and ham!"}))
        ark (get-ark ark-db)
        je-properties (get-current-property-values ark je-uuid)
        sam-uuid (name-lookup ark "Sam")
        sam-properties (get-current-property-values ark sam-uuid)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties)
    (println :sam-uuid sam-uuid)
    (println :sam-properties sam-properties)
    )

  (println)
  (println ">>>>>>>>>>>> destroy-bob")
  (println)
  (let [ark (get-ark ark-db)
        bob-uuid (name-lookup ark "Bob")
        je-uuid (process-transaction! ark-db ::destroy-rolon-transaction
                                      (prn-str bob-uuid))
        ark (get-ark ark-db)
        je-properties (get-current-property-values ark je-uuid)
        bob-properties (get-current-property-values ark bob-uuid)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties)
    (println :bob-uuid bob-uuid)
    (println :bob-properties bob-properties)
    )

  (println)
  (println ">>>>>>>>>>>> ark")
  (println)
  (println (get-ark ark-db))
  )

(deftest arks
          (println "impl0 tests")
          (test0 (impl0/create-ark-db)))
