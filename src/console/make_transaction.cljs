(ns console.make-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn make-transaction
  []
  (client/transaction!
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

(defn do-make
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Make!" client/form-name))

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt!)
               (client/add-history! ">")
               (client/add-history! "Make Bob transaction\n" client/command-prefix-style)
               (make-transaction))
      "Submit")))
