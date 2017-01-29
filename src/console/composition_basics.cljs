(ns console.composition-basics
  (:require
    [hoplon.core :as h]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn do-basics
  []
  (h/div
    (h/button
      :css {:background-color "MistyRose"}
      :click client/display-composition
      "display")
    (h/button
      :css {:background-color "MistyRose"}
      :click (fn []
               (reset! client/composition [{} []])
               (client/display-composition))
      "reset")
    (h/button
      :css {:background-color "MistyRose"}
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "Composition! transaction\n" client/command-prefix-style)
               (builder/transaction! @client/local @client/actions))
      "Submit Composition! transaction")))
