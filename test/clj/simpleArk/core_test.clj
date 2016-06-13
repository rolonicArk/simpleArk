(ns simpleArk.core-test
  (:require [clj-uuid :as uuid]))

(def uuid1 (uuid/v1))
(def uuid2 (uuid/v1))
(def uuid3 (uuid/v1))
(def uuid4 (uuid/v1))
(def uuid5 (uuid/v1))

(println "uuid1" uuid1 (uuid/get-timestamp uuid1))
(println "uuid2" uuid2 (uuid/get-timestamp uuid2))
(println "uuid3" uuid3 (uuid/get-timestamp uuid3))
(println "uuid4" uuid3 (uuid/get-timestamp uuid4))
(println "uuid5" uuid3 (uuid/get-timestamp uuid5))

(println (.compareTo uuid1 uuid2))
(println (.compareTo uuid2 uuid3))
