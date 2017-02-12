(ns console.invalid-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn invalid []
  (builder/transaction!
    {}
    (builder/build-invalid [])))

(defn do-invalid
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Invalid!" client/form-name))

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt!)
               (client/add-history! ">")
               (client/add-history! "Invalid!\n" client/command-prefix-style)
               (invalid))
      "Submit")))
