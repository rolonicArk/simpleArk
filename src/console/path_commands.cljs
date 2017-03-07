(ns console.path-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.uuid.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [simpleArk.mapish :as mapish]
    [console.rolon-commands :as rolon-commands]))

(defn list-changes!
  [ark-record]
  (let [path @client/selected-path
        uuid (suuid/create-uuid @client/selected-rolon)
        changes (arkRecord/get-changes-by-property ark-record uuid path)]
    (client/display-history!
      (-> []
          (client/add-prompt)
          (client/add-display ">")
          (client/add-display "list property changes over time for " client/command-prefix-style)
          (client/display-path ark-record path)
          (client/add-display " in ")
          (client/display-value ark-record uuid)
          (client/add-display "\n")))
    (client/clear-output!)
    (let [display (-> []
                      (client/add-display "changes over time for ")
                      (client/display-path ark-record path)
                      (client/add-display " in ")
                      (client/display-value ark-record uuid)
                      (client/add-display "\n\n"))]
      (client/display-output!
        (reduce
          (fn [display [[ts] v]]
            (let [je-uuid (arkRecord/get-journal-entry-uuid ark-record ts)]
              (-> display
                  (client/display-value ark-record je-uuid)
                  (client/add-display " ")
                  (client/display-value ark-record v)
                  (client/add-display "\n"))))
          display changes)))))

(defn do-path-commands
  []
  (h/div

    (h/div
      (h/strong "Selected path: ")
      (client/display-selected-path))

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (not= "" client/selected-rolon))
      :click (fn []
               (reset! client/display-mode 0)
               (client/explore! @client/my-ark-record (suuid/create-uuid @client/selected-rolon) @client/selected-path))
      "explore properties")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (< 0 (count client/selected-path)))
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt!)
               (client/add-history! ">")
               (client/add-history! "up\n" client/command-prefix-style)
               (swap! client/selected-path pop)
               (client/explore! @client/my-ark-record (suuid/create-uuid @client/selected-rolon) @client/selected-path))
      "up")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (< 0 (count client/selected-path)))
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt!)
               (client/add-history! ">")
               (client/add-history! "clear path selection\n" client/command-prefix-style)
               (reset! client/selected-path [])
               (client/explore! @client/my-ark-record (suuid/create-uuid @client/selected-rolon) @client/selected-path))
      "clear path selection")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (and
                         (< 0 (count client/selected-path))
                         (not= "" client/selected-rolon)
                         (some?
                           (arkRecord/get-changes-by-property
                             client/my-ark-record
                             (suuid/create-uuid client/selected-rolon)
                             client/selected-path))))
      :click (fn []
               (reset! client/display-mode 0)
               (list-changes! @client/my-ark-record))
      "list property changes over time")
    ))
