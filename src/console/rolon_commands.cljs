(ns console.rolon-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn do-rolon-commands
  []
  (h/div
    (h/div
      (h/span
        (h/strong "Selected Rolon: "))
      (h/span
        :style (j/cell= (if (= "" client/selected-rolon)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(client/alternate-click @client/my-ark-record @client/selected-rolon)
        (h/text
          (if (= "" client/selected-rolon)
            "none"
            (client/pretty-uuid client/my-ark-record (suuid/create-uuid client/selected-rolon))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" client/selected-rolon)
                         (some? (arkRecord/get-property-value
                                  client/my-ark-record
                                  (suuid/create-uuid client/selected-rolon)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" client/selected-rolon)
            (arkRecord/get-property-value
              client/my-ark-record
              (suuid/create-uuid client/selected-rolon)
              [:index/headline])))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" client/selected-rolon))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "clear rolon selection\n" client/command-prefix-style)
                 (reset! client/selected-rolon ""))
        "clear rolon selection")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/list-current-micro-properties @client/my-ark-record))
        "list current micro-properties")

      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not (suuid/journal-entry-uuid? (suuid/create-uuid client/selected-rolon))))
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/list-modifying-transactions @client/my-ark-record))
        "list modifying transactions")
      )
    ))
