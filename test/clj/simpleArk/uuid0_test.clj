(ns simpleArk.uuid0-test
  (:require [clojure.test :refer :all]
            [simpleArk.uuid :refer :all]
            [simpleArk.uuid0 :as uuid0]))

(set! *warn-on-reflection* true)

(def ark-db ((comp
               (uuid0/builder))
              {}))

(deftest uuid0
  (def je-uuid0 (journal-entry-uuid ark-db))
  (def random-uuid0 (random-uuid ark-db))
  (def index-uuid0 (index-uuid ark-db :index/z))
  (is (thrown? Exception (index-uuid ark-db 1)))
  (is (thrown? Exception (index-uuid ark-db :content/y)))

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
  (is (not (index-uuid? random-uuid0))))
