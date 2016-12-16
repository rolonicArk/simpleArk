(ns console.commands-view
  (:require
    [hoplon.core :as h]
    [console.time-commands :as time-commands]
    [console.index-commands :as index-commands]
    [console.rolon-commands :as rolon-commands]
    [console.alternate-commands :as alternate-commands]
    [console.count-commands :as count-commands]
    [console.transaction-commands :as transaction-commands]))

(defn do-commands
  []
  (h/div
    (time-commands/do-times-commands)
    (h/hr)
    (index-commands/do-index-commands)
    (h/hr)
    (rolon-commands/do-rolon-commands)
    (h/hr)
    (alternate-commands/do-alternate-commands)
    (h/hr)
    (count-commands/do-count-commands)
    (h/hr)
    (transaction-commands/do-transaction-commands)
    ))
