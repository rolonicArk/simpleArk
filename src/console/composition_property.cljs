(ns console.composition-property
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def property-uuid (j/cell ""))

(def property-path (j/cell ""))

(def property-value (j/cell ""))

(defn do-property
  []
  (h/form
    :submit (fn []
              (swap! client/actions builder/build-property
                     (client/read-cell property-uuid)
                     (client/read-cell property-path)
                     (client/read-cell property-value))
              (client/display-composition))
    (h/label "Add a property")
    (h/div
      (h/label "Rolon: ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value property-uuid
               :keyup #(reset! property-uuid @%)))
    (h/div
      (h/label "Path: ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value property-path
               :keyup #(reset! property-path @%)))
    (h/div
      (h/label "Value: ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value property-value
               :keyup #(reset! property-value @%)))
    (h/div
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (and
                           (not= "" property-uuid)
                           (not= "" property-path)
                           (not= "" property-value)
                           ))
        :type "submit"
        "OK"))))
