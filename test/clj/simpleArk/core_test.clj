(ns simpleArk.core-test
  (:require [clojure.test :refer :all]
            [clj-uuid :as uuid]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]))

(defn hello-world
  "simple transaction test"
  [ark je-uuid s]
  (println "Hello," s)
  (let [ark (update-property ark je-uuid je-uuid :descriptor:headline "Just for fun!")]
    ark))

(defn test0
  "tests that even work with impl0"
  [ark-db]
  (register-transaction! ark-db ::hello-world hello-world)
  (let [je1-uuid (process-transaction! ark-db ::hello-world "Fred")
        ark (get-ark ark-db)
        je1 (get-rolon ark je1-uuid)
        latest-je1-value (get-latest-rolon-value je1)
        properties (get-property-values latest-je1-value)]
    (println :transaction-properties properties)))

(deftest arks
          (println "impl0 tests")
          (test0 (impl0/create-ark-db)))
