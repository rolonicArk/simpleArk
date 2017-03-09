(ns console.composition-basics
  (:require
    [hoplon.core :as h]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn do-basics
  [resets]
  (h/div
    (h/button
      :css {:background-color "MistyRose"}
      :click client/output-composition!
      "display")
    (h/button
      :css {:background-color "MistyRose"}
      :click (fn []
               (resets)
               (client/output-composition!))
      "reset")
    (h/button
      :css {:background-color "MistyRose"}
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt!)
               (client/add-history! ">")
               (client/add-history! "Composition! transaction\n" client/command-prefix-style)
               (client/transaction! @client/local @client/actions))
      "Submit Composition! transaction")))
