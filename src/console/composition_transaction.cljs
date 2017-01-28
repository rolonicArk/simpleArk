(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def composition (j/cell [{}[]]))

(def local (j/cell=
             (first composition)
             (fn [new-local]
               (swap!
                 composition
                 (fn [old-composition]
                   [new-local (second old-composition)])))))

(def actions (j/cell=
             (second composition)
             (fn [new-actions]
               (swap!
                 composition
                 (fn [old-composition]
                   [(first old-composition) new-actions])))))

(defn do-composition
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Composition!" client/form-name))
    (h/div
      (h/button
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/clear-output!)
                 (client/add-output! "Composed Transaction")
                 (client/output-tran! @client/my-ark-record @composition))
        "display")
      )
    (h/hr)
    ))
