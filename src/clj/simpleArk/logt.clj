(ns simpleArk.logt
  (:require [simpleArk.log :as log]
            [clojure.core.async :as async]))

(set! *warn-on-reflection* true)

(defn fmt
  "format a log message"
  [this msgs]
  (first msgs))

(defn set-log-chan
  [this chan]
  (assoc this ::chan chan))

(defn get-msg
  [this]
  (async/<!! (::chan this)))

(defn info
  [this & s]
  (let [msg (log/fmt this s)]
    (async/>!! (::chan this) [:log/info msg])))

(defn warn
  [this & s]
  (let [msg (log/fmt this s)]
    (async/>!! (::chan this) [:log/warn msg])))

(defn debug
  [this & s]
  (let [msg (log/fmt this s)]
    (async/>!! (::chan this) [:log/debug msg])))

(defn error
  [this & s]
  (let [msg (log/fmt this s)]
    (async/>!! (::chan this) [:log/error msg])))

(defn build
  "build a logt component"
  [m]
  (-> m
      (assoc :log/fmt fmt)
      (assoc :log/info info)
      (assoc :log/warn warn)
      (assoc :log/debug debug)
      (assoc :log/error error)))
