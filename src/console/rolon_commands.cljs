(ns console.rolon-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.mapish :as mapish]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn list-current-micro-properties
  [ark-record]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "list current micro-properties\n" client/command-prefix-style)
  (client/clear-output!)
  (client/add-output! "current micro-properties of ")
  (let [uuid (suuid/create-uuid @client/selected-rolon)
        properties (arkRecord/get-property-values ark-record uuid)]
    (client/add-output! (client/pretty-uuid ark-record uuid) (client/clickable-styles uuid) client/uuid-click @client/selected-rolon)
    (client/add-output! ":\n\n")
    (reduce
      (fn [_ [path value]]
        (client/format-path ark-record path)
        (client/add-output! " ")
        (client/add-output! (pr-str value))
        (client/add-output! "\n\n"))
      nil properties)))

(defn list-modifying-transactions
  [ark-record]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "list modifying transactions\n" client/command-prefix-style)
  (client/clear-output!)
  (client/add-output! "transactions that modifified ")
  (let [uuid (suuid/create-uuid @client/selected-rolon)
        all-properties (arkRecord/get-property-values ark-record uuid)
        properties (mapish/mi-sub all-properties [:inv-rel/modified])]
    (client/add-output! (client/pretty-uuid ark-record uuid)
                        (client/clickable-styles uuid)
                        client/uuid-click
                        @client/selected-rolon)
    (client/add-output! "\n\n")
    (reduce
      (fn [_ [path value]]
        (let [k (nth path 1)
              u (arkRecord/get-journal-entry-uuid ark-record k)]
          (client/add-output! (client/pretty-uuid ark-record u) (client/clickable-styles u) client/uuid-click (str u))
          (let [headline (arkRecord/get-property-value
                           ark-record
                           u
                           [:index/headline])]
            (if (some? headline)
              (client/add-output! (str " - " headline)))))
        (client/add-output! "\n"))
      nil properties)))

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
                 (list-current-micro-properties @client/my-ark-record))
        "list current micro-properties")

      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not (suuid/journal-entry-uuid? (suuid/create-uuid client/selected-rolon))))
        :click (fn []
                 (reset! client/display-mode 0)
                 (list-modifying-transactions @client/my-ark-record))
        "list modifying transactions")
      )
    ))
