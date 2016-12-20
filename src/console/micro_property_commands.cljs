(ns console.micro-property-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]))

(defn list-changes
  [ark-record]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "list changes over time for " client/command-prefix-style)
  (let [path @client/selected-microp
        uuid (suuid/create-uuid @client/selected-rolon)
        changes (arkRecord/get-changes-by-property ark-record uuid path)]
    (client/history-path! ark-record path)
    (client/add-history! " in ")
    (client/add-history!
      (client/pretty-uuid ark-record uuid)
      (client/clickable-styles uuid)
      client/uuid-click
      @client/selected-rolon)
    (client/add-history! "\n")
    (client/clear-output!)
    (client/add-output! "changes over time for ")
    (client/output-path! ark-record path)
    (client/add-output! " in ")
    (client/add-output!
      (client/pretty-uuid ark-record uuid)
      (client/clickable-styles uuid)
      client/uuid-click
      @client/selected-rolon)
    (client/add-output! "\n\n")
    (reduce
      (fn [_ [[ts] v]]
        (client/add-output! (client/pretty-uuid
                              ark-record
                              (arkRecord/get-journal-entry-uuid ark-record ts)))
        (client/add-output! " ")
        (client/add-output! (pr-str v))
        (client/add-output! "\n"))
      nil changes)
  ))

(defn do-micro-property-commands
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (< 0 (count client/selected-microp)))

    (h/div
      (h/strong "Selected micro-property: ")
      (client/display-selected-path))

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "clear micro-property selection\n" client/command-prefix-style)
               (reset! client/selected-microp []))
      "clear micro-property selection")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (not= "" client/selected-rolon))
      :click (fn []
               (reset! client/display-mode 0)
               (list-changes @client/my-ark-record))
      "list changes over time")
    ))
