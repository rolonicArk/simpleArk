(ns console.index-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [console.client :as client]))

(defn display-index
  [ark-record content-index index-uuid]
  (let [name (arkRecord/get-property-value ark-record index-uuid [:index/index.name])]
    (doall (map (fn [kv]
                  (let [k (str (key kv) " ")
                        v (val kv)]
                    (case name
                      "index.name" ()
                      "name" ()
                      (client/add-output! k client/bold-style))
                    (client/add-output! (client/pretty-uuid ark-record v)
                                 (client/clickable-styles v) client/uuid-click (str v))
                    (if (not= name "headline")
                      (let [headline (arkRecord/get-property-value
                                       ark-record
                                       (suuid/create-uuid v)
                                       [:index/headline])]
                        (if (some? headline)
                          (client/add-output! (str " - " headline)))))
                    (client/add-output! "\n")))
                content-index))))

(defn list-index-content [ark-record index-uuid]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "list index content\n" client/command-prefix-style)
  (client/clear-output!)
  (client/add-output! "index: ")
  (client/add-output! (str (client/pretty-uuid ark-record index-uuid) "\n")
               (client/clickable-styles index-uuid)
                      client/uuid-click
               (str index-uuid))
  (let [content-index (arkRecord/get-content-index
                        ark-record
                        index-uuid)]
    (display-index ark-record content-index index-uuid)))

(defn do-index-commands
  []
  (h/div
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
                 (list-index-content @client/my-ark-record arkRecord/index-name-uuid))
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
                 (list-index-content @client/my-ark-record
                                            (suuid/create-uuid @client/selected-index)))
        "list index content"))
    ))
