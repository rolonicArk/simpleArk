(ns console.micro-property-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]))

(defn do-micro-property-commands
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not= false client/selected-microp))
    (h/div
      (h/span
        (h/strong
          "Selected micro-property: "))
      (h/span
        (h/text
          (if (not= false client/selected-microp)
            (client/path-str client/my-ark-record client/selected-microp)
            "")))
      )
    ))
