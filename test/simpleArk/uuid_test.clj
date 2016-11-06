(ns simpleArk.uuid-test
  (:require [clojure.test :refer :all]
            [clj-uuid :as uuid]))

(set! *warn-on-reflection* true)

(def uuid0 (java.util.UUID/randomUUID))

(def uuid1 (uuid/v1))
(def uuid2 (uuid/v1))
(def uuid3 (uuid/v1))

(deftest uuids
  (is (= (uuid/get-version uuid0) 4))
  (is (nil? (uuid/get-timestamp uuid0)))

  (is (= (uuid/get-version uuid1) 1))
  (is (uuid/get-timestamp uuid1))

  (is (= (compare uuid1 uuid2) -1))
  (is (= (compare uuid2 uuid3) -1))

  (is (uuid? uuid1))
  (is (uuid/uuid? uuid0))
  )
