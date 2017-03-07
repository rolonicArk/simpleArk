(ns console.count-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn application-rolons-count [ark-record]
  (client/add-prompt!)
  (client/add-history! ">")
  (client/add-history! "application rolons count:" client/command-prefix-style)
  (client/add-history! " ")
  (client/add-history! (str (count (arkRecord/get-application-rolons ark-record)) "\n")))

(defn indexes-count [ark-record]
  (client/add-prompt!)
  (client/add-history! ">")
  (client/add-history! "index rolons count:" client/command-prefix-style)
  (client/add-history! " ")
  (client/add-history! (str (count (arkRecord/get-indexes ark-record)) "\n")))

(defn je-count [ark-record]
  (client/add-prompt!)
  (client/add-history! ">")
  (client/add-history! "transactions count:" client/command-prefix-style)
  (client/add-history! " ")
  (client/add-history! (str (count (arkRecord/get-journal-entries ark-record)) "\n")))

(defn do-count-commands
  []
  (h/div
    (h/div

      (h/output (h/strong "Rolon Counts: "))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (application-rolons-count @client/my-ark-record))
        "applications")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (indexes-count @client/my-ark-record))
        "indexes")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (je-count @client/my-ark-record))
        "transactions"))
    ))
