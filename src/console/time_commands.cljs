(ns console.time-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn do-times-commands
  []
  (h/div
    (h/div
      (h/span
        (h/strong
          "Selected time: "))
      (h/span
        :style (j/cell= (if (= "" client/selected-time)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(client/rolon-click! @client/my-ark-record @client/selected-time)
        (h/text
          (if (= "" client/selected-time)
            "now"
            (client/pretty-value client/my-ark-record (suuid/create-uuid client/selected-time))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" client/selected-time)
                         (some? (arkRecord/get-property-value
                                  client/my-ark-record
                                  (suuid/create-uuid client/selected-time)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" client/selected-time)
            (arkRecord/get-property-value
              client/my-ark-record
              (suuid/create-uuid client/selected-time)
              [:index/headline])))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" client/selected-time))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt!)
                 (client/add-history! ">")
                 (client/add-history! "clear time selection\n" client/command-prefix-style)
                 (reset! client/selected-time ""))
        "clear time selection")
      )

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (some? client/transaction-je-uuid-string))
      (h/span
        (h/strong "My last Transaction: "))
      (h/span
        :style "color:orange;cursor:pointer"
        :click #(client/uuid-click! @client/my-ark-record @client/transaction-je-uuid-string)
        (h/text (client/pretty-value client/my-ark-record (suuid/create-uuid client/transaction-je-uuid-string)))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (some? client/latest-journal-entry-uuid))
      (h/span
        (h/strong "Latest Transaction: "))
      (h/span
        :style "color:orange;cursor:pointer"
        :click #(client/uuid-click! @client/my-ark-record (str @client/latest-journal-entry-uuid))
        (h/text
          (client/pretty-value client/my-ark-record client/latest-journal-entry-uuid)))
      )
    ))
