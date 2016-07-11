;;Log component api
(ns simpleArk.log)

(set! *warn-on-reflection* true)

(defn fmt
  "format a log message"
  [this msgs]
  ((:log/fmt this) this msgs))

(defn info
  [this & s]
  ((:log/info this) this s))

(defn warn
  [this & s]
  ((:log/warn this) this s))

(defn debug
  [this & s]
  ((:log/debug this) this s))

(defn error
  [this & s]
  ((:log/error this) this s))
