(ns console.commands-view
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [tiples.client :as tiples]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn do-commands
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
        :click #(client/rolon-click @client/my-ark-record @client/selected-time)
        (h/text
          (if (= "" client/selected-time)
            "now"
            (client/pretty-uuid client/my-ark-record (suuid/create-uuid client/selected-time))))))

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
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "clear time selection\n" client/command-prefix-style)
                 (reset! client/selected-time ""))
        "clear time selection")
      )

    (h/hr)

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (some? client/transaction-je-uuid-string))
      (h/span
        (h/strong "My last Transaction: "))
      (h/span
        :style "color:orange;cursor:pointer"
        :click #(client/uuid-click @client/my-ark-record @client/transaction-je-uuid-string)
        (h/text (client/pretty-uuid client/my-ark-record (suuid/create-uuid client/transaction-je-uuid-string)))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (some? client/latest-journal-entry-uuid))
      (h/span
        (h/strong "Latest Transaction: "))
      (h/span
        :style "color:orange;cursor:pointer"
        :click #(client/uuid-click @client/my-ark-record (str @client/latest-journal-entry-uuid))
        (h/text
          (client/pretty-uuid client/my-ark-record client/latest-journal-entry-uuid)))
      )

    (h/hr)

    (h/div
      (h/span
        (h/strong "Selected Index: "))
      (h/span
        :style (j/cell= (if (= "" client/selected-index)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(client/rolon-click @client/my-ark-record @client/selected-index)
        (h/text
          (if (= "" client/selected-index)
            "none"
            (client/pretty-uuid client/my-ark-record (suuid/create-uuid client/selected-index))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" client/selected-index)
                         (some? (arkRecord/get-property-value
                                  client/my-ark-record
                                  (suuid/create-uuid client/selected-index)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" client/selected-index)
            (arkRecord/get-property-value
              client/my-ark-record
              (suuid/create-uuid client/selected-index)
              [:index/headline])))))

    (h/div

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/list-index-content @client/my-ark-record arkRecord/index-name-uuid))
        "list indexes")

      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" client/selected-index))
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "clear index selection\n" client/command-prefix-style)
                 (reset! client/selected-index ""))
        "clear index selection")

      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" client/selected-index))
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/list-index-content @client/my-ark-record
                                     (suuid/create-uuid @client/selected-index)))
        "list index content"))

    (h/hr)

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

    (h/hr)

    (h/div
      (h/span
        (h/strong "Alternate Rolon: "))
      (h/span
        :style (j/cell= (if (= "" client/alternate-rolon)
                          ""
                          "color:YellowGreen;cursor:pointer"
                          ))
        :click #(client/rolon-click @client/my-ark-record @client/alternate-rolon)
        (h/text
          (if (= "" client/alternate-rolon)
            "none"
            (client/pretty-uuid client/my-ark-record (suuid/create-uuid client/alternate-rolon))))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         (not= "" client/alternate-rolon)
                         (some? (arkRecord/get-property-value
                                  client/my-ark-record
                                  (suuid/create-uuid client/alternate-rolon)
                                  [:index/headline]))))
      (h/text
        (str
          "headline: "
          (if (not= "" client/alternate-rolon)
            (arkRecord/get-property-value
              client/my-ark-record
              (suuid/create-uuid client/alternate-rolon)
              [:index/headline])))))

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" client/alternate-rolon))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "clear alternate selection\n" client/command-prefix-style)
                 (reset! client/alternate-rolon ""))
        "clear alternate selection")

      (h/button
        :style "background-color:MistyRose"
        :css {:display "none"}
        :toggle (j/cell= (not= client/selected-rolon client/alternate-rolon))
        :click (fn []
                 (reset! client/display-mode 0)
                 (let [r @client/selected-rolon
                       a @client/alternate-rolon]
                   (client/rolon-click @client/my-ark-record a)
                   (client/alternate-click @client/my-ark-record r)))
        "swap with rolon selection")
      )

    (h/hr)

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

    (h/div
      :css {:display "none"}
      :toggle (j/cell= (and
                         client/channel-open
                         (= "" client/selected-time)))

      (h/output (h/strong "Transactions: "))

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "Hello Fred transaction\n" client/command-prefix-style)
                 (client/fred))
        :href ""
        "Hello Fred")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "Make Bob transaction\n" client/command-prefix-style)
                 (client/make-bob))
        :href ""
        "Make Bob")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "Invalid!\n" client/command-prefix-style)
                 (tiples/chsk-send! [:console/process-transaction {:tran-keyword :invalid :tran-data ""}]))
        "Invalid!")

      (h/button
        :style "background-color:MistyRose"
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "Trouble!\n" client/command-prefix-style)
                 (tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}]))
        "Trouble!"))

    (h/div
      :style "color:red"
      (h/p (h/text (if client/transaction-error
                     (str "Error: " client/transaction-error-msg)
                     ""))))))
