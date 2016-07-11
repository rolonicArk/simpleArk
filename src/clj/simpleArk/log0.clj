(ns simpleArk.log0
  (:require [simpleArk.log :as log]))

(set! *warn-on-reflection* true)

(defn fmt
  "format a log message"
  [this msgs]
  (apply str (interpose " " (map pr-str msgs))))

(defn info
  [this & s]
  (let [msg (log/fmt this s)]
    (println :log/info msg)))

(defn warn
  [this & s]
  (let [msg (log/fmt this s)]
    (println :log/warn msg)))

(defn debug
  [this & s]
  (let [msg (log/fmt this s)]
    (println :log/debug msg)))

(defn error
  [this & s]
  (let [msg (log/fmt this s)]
    (println :log/error msg)))

(defn build
  "build a log0 component"
  [m]
  (-> m
      (assoc :log/fmt fmt)
      (assoc :log/info info)
      (assoc :log/warn warn)
      (assoc :log/debug debug)
      (assoc :log/error error)))
