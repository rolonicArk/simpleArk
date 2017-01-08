(ns console.alternate-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn do-alternate-commands
  []
  (h/div
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
            (client/pretty-value client/my-ark-record (suuid/create-uuid client/alternate-rolon))))))

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
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= client/selected-rolon client/alternate-rolon))
        :click (fn []
                 (reset! client/display-mode 0)
                 (let [r @client/selected-rolon
                       a @client/alternate-rolon]
                   (client/rolon-click @client/my-ark-record a)
                   (client/alternate-click @client/my-ark-record r)))
        "swap with rolon selection")
      )
    ))
