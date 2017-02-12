(ns console.trouble-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn trouble [msg]
  (builder/transaction!
    {}
    (builder/build-exception [] msg)))

(defn do-trouble
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Trouble!" client/form-name))

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt!)
               (client/add-history! ">")
               (client/add-history! "Trouble!\n" client/command-prefix-style)
               (trouble "A troublesome transaction"))
      "Submit")))
