(ns simpleArk.closer-test
  (:require [clojure.test :refer :all]
            [simpleArk.log :refer :all]
            [simpleArk.logt :as logt]
            [clojure.core.async :as async]
            [simpleArk.closer :refer :all]))

(set! *warn-on-reflection* true)

(defn dummy-builder [& {:keys [name]}]
  (fn [m]
    (open-component m
                    name
                    #(info! % (str "close " name)))))

(deftest closer
  (let [this ((comp
                (dummy-builder :name "c")
                (dummy-builder :name "b")
                (dummy-builder :name "a")
                (logt/builder)) {})]
    (is (= [:log/info! "opening a"] (logt/get-msg this)))
    (is (= [:log/info! "opening b"] (logt/get-msg this)))
    (is (= [:log/info! "opening c"] (logt/get-msg this)))
    (close-all this)
    (is (= [:log/info! "closing c"] (logt/get-msg this)))
    (is (= [:log/info! "close c"] (logt/get-msg this)))
    (is (= [:log/info! "closing b"] (logt/get-msg this)))
    (is (= [:log/info! "close b"] (logt/get-msg this)))
    (is (= [:log/info! "closing a"] (logt/get-msg this)))
    (is (= [:log/info! "close a"] (logt/get-msg this)))
    ))
