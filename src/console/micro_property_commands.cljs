(ns console.micro-property-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]))

(defn list-changes
  [ark-record]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "list changes over time\n" client/command-prefix-style)
  )

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
