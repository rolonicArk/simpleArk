(ns console.transaction-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [tiples.client :as tiples]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn hello [name]
  (builder/transaction!
    {}
    (-> []
        (builder/build-println
          (str "Hello " name "!"))
        (builder/build-je-property
          [:index/headline] "Just for fun!"))))

(defn make-bob []
  (tiples/chsk-send!
    [:console/process-transaction
     {:tran-keyword
      :ark/update-rolon-transaction!
      :tran-data
      (prn-str
        [nil
         {[:index/headline] "make bob"}
         {[:content/age]             8
          [:index/name]              "Bob"
          [:index/headline]          "First application Rolon"
          [:content/brothers "John"] true
          [:content/brothers "Jeff"] true}])
      }]))

(defn invalid []
  (builder/transaction!
    {}
    (builder/build-invalid [])))

(defn do-transaction-commands
  []
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
               (hello "Fred"))
      :href ""
      "Hello Fred")

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "Make Bob transaction\n" client/command-prefix-style)
               (make-bob))
      :href ""
      "Make Bob")

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "Invalid!\n" client/command-prefix-style)
               (invalid))
      "Invalid!")

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "Trouble!\n" client/command-prefix-style)
               (tiples/chsk-send! [:console/process-transaction {:tran-keyword :trouble! :tran-data ""}]))
      "Trouble!")

    (h/div
      :style "color:red"
      (h/p (h/text (if client/transaction-error
                     (str "Error: " client/transaction-error-msg)
                     "")))))
  )
