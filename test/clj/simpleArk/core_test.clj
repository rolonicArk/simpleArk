(ns simpleArk.core-test
  (:require [clojure.test :refer :all]
            [clj-uuid :as uuid]
            [simpleArk.core :refer :all]
            [simpleArk.impl0 :as impl0]))

(defn hello-world
  "simple transaction test"
  [ark je s]
  (println "Hello," s)
  ark)

(defn test0
  "tests that even work with impl0"
  [ark-db]
  (register-transaction! ark-db ::hello-world hello-world)
  (let [jeuuid1 (process-transaction! ark-db ::hello-world "Fred")]
    (println jeuuid1)))

(deftest arks
          (println "impl0 tests")
          (test0 (impl0/create-ark-db)))
