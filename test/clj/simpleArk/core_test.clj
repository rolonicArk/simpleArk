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

(defn hello-world!
  "simple transaction test"
  [s]
  (println "Hello," s)
  (let [je-uuid (get-current-journal-entry-uuid)]
    (update-property! je-uuid :classifier/headline "Just for fun!")))

(defn test0
  "tests that even work with impl0"
  [ark-db]
  (register-transaction! ark-db ::hello-world! hello-world!)
  (register-transaction! ark-db ::update-rolon-transaction! update-rolon-transaction!)
  (register-transaction! ark-db ::destroy-rolon-transaction! destroy-rolon-transaction!)

  (println)
  (println ">>>>>>>>>>>> hello-world")
  (println)
  (def hello-je-uuid (process-transaction! ark-db ::hello-world! "Fred"))
  (println :hello-je-uuid hello-je-uuid)
  (bind-ark ark-db
            (println :je-properties (get-current-property-values hello-je-uuid)))

  (println)
  (println ">>>>>>>>>>>> make-bob")
  (println)
  (def bob-uuid (random-uuid))
  (println :bob-uuid bob-uuid)
  (def make-bob-je-uuid (process-transaction! ark-db ::update-rolon-transaction!
                                              (prn-str [bob-uuid
                                                        {:classifier/headline "make bob"}
                                                        {:descriptor/age 8 :classifier/name "Bob"}])))
  (println :make-bob-je-uuid make-bob-je-uuid)
  (bind-ark ark-db
    (println :je-properties (get-current-property-values make-bob-je-uuid))
    (println :bob-properties (get-current-property-values bob-uuid)))

  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "update 1"}
                                  {:classifier/headline "kissing is gross!"}]))

  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "update 2"}
                                  {:classifier/headline "who likes girls?"}]))

  (process-transaction! ark-db ::update-rolon-transaction!
                        (prn-str [bob-uuid
                                  {:classifier/headline "update 3"}
                                  {:classifier/headline "when do I get my own mobile!"}]))

  (println)
  (println ">>>>>>>>>>>> make-sam")
  (println)
  (def sam-uuid (random-uuid))
  (println :sam-uuid sam-uuid)
  (def make-sam-je-uuid (process-transaction! ark-db ::update-rolon-transaction!
                                              (prn-str [sam-uuid
                                                        {:classifier/headline "make sam"}
                                                        {:descriptor/age 10
                                                         :classifier/name "Sam"
                                                         :classifier/headline "I hate green eggs and ham!"}])))
  (println :make-sam-je-uuid make-sam-je-uuid)
  (bind-ark ark-db
    ;(println :je-properties (get-current-property-values make-sam-je-uuid))
    (println :sam-properties (get-current-property-values sam-uuid)))

  (println)
  (println ">>>>>>>>>>>> destroy-bob")
  (println)
  (def destroy-bob-je-uuid (process-transaction! ark-db ::destroy-rolon-transaction!
                                                 (prn-str [bob-uuid
                                                {:classifier/headline "destroy bob"}])))
  (println :destroy-bob-je-uuid destroy-bob-je-uuid)
  (bind-ark ark-db
            ;(println :je-properties (get-current-property-values je-uuid))
            (println :bob-properties (get-current-property-values bob-uuid))
            (println :lookup-bob (name-lookup "Bob")))

  (println)
  (println ">>>>>>>>>> select time: make-bob-je-uuid")
  (println)
  (bind-ark ark-db
            (select-time! make-bob-je-uuid)
            (println :bob-properties (get-current-property-values bob-uuid))
            (println :lookup-bob (name-lookup "Bob")))

  (println)
  (println ">>>>>>>>>>>> journal entry headlines")
  (println)
  (bind-ark ark-db
            (first (keep (fn [x] (println (get-current-property-value (key x) :classifier/headline)))
                         (get-journal-entries))))

(println)
(println ">>>>>>>>>>>> all the latest headlines")
(println)
(bind-ark ark-db
          (let [headline-index-uuid (get-index-uuid "headline")
                current-rolon-value (get-current-property-values headline-index-uuid)
                descriptor-index (:descriptor/index current-rolon-value)]
            (first (keep (fn [x]
                           (if (first (val x))
                             (println (key x))))
                         descriptor-index))
            )))

(deftest arks
          (println "impl0 tests")
          (test0 (impl0/create-ark-db)))
