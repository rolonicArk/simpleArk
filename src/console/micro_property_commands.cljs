(ns console.micro-property-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]))

(defn do-micro-property-commands
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (< 0 (count client/selected-microp)))
    (h/div
      (h/strong "Selected micro-property: ")
      (client/display-selected-path))))
