(ns console.transaction-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn do-transaction-commands
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (and
                       client/channel-open
                       (= "" client/selected-time)))

    (h/output (h/strong "Transactions: "))

    (h/button
      :click (fn []
               (reset! client/form-name "Hello world!")
               (reset! client/display-mode 0))
      :href ""
      "Hello world!")

    (h/button
      :click (fn []
               (reset! client/form-name "Make!")
               (reset! client/display-mode 0))
      :href ""
      "Make!")

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

    (h/button
      :click (fn []
               (reset! client/form-name "Composition!")
               (reset! client/display-mode 0))
      "Composition!")
    ))
