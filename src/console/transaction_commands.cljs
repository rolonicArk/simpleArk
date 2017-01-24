(ns console.transaction-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
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

(defn make-bob-transaction
  []
  (builder/transaction!
    {}
    (-> []
        (builder/build-je-property
          [:index/headline] "make bob")
        (builder/build-gen-uuid
          :local/bob-uuid)
        (builder/build-property
          :local/bob-uuid [:index/headline] "First application Rolon")
        (builder/build-property
          :local/bob-uuid [:content/age] 8)
        (builder/build-property
          :local/bob-uuid [:index/name] "Bob")
        (builder/build-property
          :local/bob-uuid [:content/brothers "John"] true)
        (builder/build-property
          :local/bob-uuid [:content/brothers "Jeff"] true)
        )))

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
               (make-bob-transaction))
      :href ""
      "Make Bob")

    (h/button
      :click (fn []
               (reset! client/form-name "Invalid!")
               (reset! client/display-mode 0))
      "Invalid!")

    (h/button
      :click (fn []
               (reset! client/form-name "Trouble!")
               (reset! client/display-mode 0))
      "Trouble!")
    ))
