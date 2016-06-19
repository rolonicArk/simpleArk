(ns simpleArk.core-test
  (:require [clojure.test :refer :all]
            [clj-uuid :as uuid]
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
  (let [je-uuid (get-latest-journal-entry-uuid ark)
        ark (update-property ark je-uuid :classifier/headline "Just for fun!")]
    ark))

(defn make-bob
  "creates the rolon, Bob"
  [ark s]
  (let [je-uuid (get-latest-journal-entry-uuid ark)
        ark (update-property ark je-uuid :classifier/headline "creates the rolon, Bob")
        bob-uuid (random-uuid)
        ark (create-rolon ark bob-uuid (sorted-map :descriptor/age 8 :classifier/name "Bob"))]
    (println :bob-uuid bob-uuid)
    ark))

(defn destroy-something
  "destroys a rolon"
  [ark s]
  (let [je-uuid (get-latest-journal-entry-uuid ark)
        ark (update-property ark je-uuid :classifier/headline "destroys a non-je rolon")
        other (get-other-rolons ark)
        [bob-uuid bob] (first other)
        ark (destroy-rolon ark bob-uuid)]
    ark))

(defn test0
  "tests that even work with impl0"
  [ark-db]
  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)
  (register-transaction! ark-db ::hello-world hello-world)
  (let [je-uuid (process-transaction! ark-db ::hello-world "Fred")
        ark (get-ark ark-db)
        je (get-rolon ark je-uuid)
        je-properties (get-latest-property-values je)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties))

  (println)
  (println ">>>>>>>>>>>> make-bob")
  (println)
  (register-transaction! ark-db ::make-bob make-bob)
  (let [je-uuid (process-transaction! ark-db ::make-bob "")
        ark (get-ark ark-db)
        je (get-rolon ark je-uuid)
        je-properties (get-latest-property-values je)
        other (get-other-rolons ark)
        [bob-uuid bob] (first other)
        bob-properties (get-latest-property-values bob)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties)
    (println :bob-uuid bob-uuid)
    (println :bob-properties bob-properties))

  (println)
  (println ">>>>>>>>>>>> destroy-something")
  (println)
  (register-transaction! ark-db ::destroy-something destroy-something)
  (let [je-uuid (process-transaction! ark-db ::destroy-something "")
        ark (get-ark ark-db)
        je (get-rolon ark je-uuid)
        je-properties (get-latest-property-values je)
        other (get-other-rolons ark)
        [bob-uuid bob] (first other)
        bob-properties (get-latest-property-values bob)]
    (println :je-uuid je-uuid)
    (println :transaction-properties je-properties)
    (println :bob-uuid bob-uuid)
    (println :bob-properties bob-properties)))

(deftest arks
          (println "impl0 tests")
          (test0 (impl0/create-ark-db)))
