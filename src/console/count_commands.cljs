(ns console.count-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn do-count-commands
  []
  (h/div
    (h/div

      (h/output (h/strong "Rolon Counts: "))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/application-rolons-count @client/my-ark-record))
        "applications")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/indexes-count @client/my-ark-record))
        "indexes")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/je-count @client/my-ark-record))
        "transactions"))
    ))
