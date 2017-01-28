(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]
    [simpleArk.uuid :as suuid]))

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

(def parameter-name (j/cell ""))

(defn display-composition
  []
  (reset! client/display-mode 0)
  (client/clear-output!)
  (client/add-output! "Composed Transaction")
  (client/output-tran! @client/my-ark-record @composition))

(defn do-composition
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Composition!" client/form-name))
    (h/div
      (h/button
        :css {:background-color "MistyRose"}
        :click display-composition
        "display")
      (h/button
        :css {:background-color "MistyRose"}
        :click (fn []
                 (reset! composition [{} []])
                 (display-composition))
        "reset")
      )
    (h/hr)
    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" client/selected-rolon))
      (h/form
        :submit (fn []
                  (swap! local
                         (fn [old]
                           (assoc old
                             (keyword "local" @parameter-name)
                             (suuid/create-uuid @client/selected-rolon))))
                  (display-composition))
        (h/label "Add Selected Rolon as parameter ")
        (h/input :type "text"
                 :value parameter-name
                 :keyup #(reset! parameter-name @%))
        (h/label " ")
        (h/button
          :css {:display "none" :background-color "MistyRose"}
          :toggle (j/cell= (not= "" parameter-name))
          :type "submit"
          "OK")))
    (h/hr)
    ))
