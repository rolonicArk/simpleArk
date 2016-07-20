(ns simpleArk.uuidi-test
  (:require [clojure.test :refer :all]
            [simpleArk.uuid :refer :all]
            [simpleArk.uuidi :as uuidi]))

(set! *warn-on-reflection* true)

(deftest uuid0
  (def ark-db ((comp
                 (uuidi/builder))
                {}))
  (def je-uuid0 (journal-entry-uuid ark-db))
  (def random-uuid0 (random-uuid ark-db))
  (def index-uuid0 (index-uuid ark-db :classifier/z))

  (is (journal-entry-uuid? je-uuid0))
  (is (random-uuid? random-uuid0))
  (is (index-uuid? index-uuid0))

  (is (= 1 (uuidi/lsw random-uuid0)))
  (is (= 2 (uuidi/lsw (random-uuid ark-db))))
  (is (= 3 (uuidi/lsw (random-uuid ark-db))))
  (is (= 4 (uuidi/lsw (random-uuid ark-db)))))
