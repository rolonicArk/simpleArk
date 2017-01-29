(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]
    [console.composition-basics :as composition-basics]
    [console.composition-selected :as composition-selected]
    [console.composition-alternate :as composition-alternate]))

(def gen-uuid-name (j/cell ""))

(def println-edn-string (j/cell ""))

(def property-uuid (j/cell ""))

(def property-path (j/cell ""))

(def property-value (j/cell ""))

(defn do-composition
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Composition!" client/form-name))
    (composition-basics/do-basics)
    (h/hr)
    (composition-selected/do-selected)
    (composition-alternate/do-alternate)
    (h/hr)
    (h/form
      :submit (fn []
                (swap! client/actions builder/build-gen-uuid @gen-uuid-name)
                (client/display-composition))
      (h/label "Add gen-uuid to :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value gen-uuid-name
               :keyup #(reset! gen-uuid-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" gen-uuid-name))
        :type "submit"
        "OK"))
    (h/form
      :submit (fn []
                (swap! client/actions builder/build-println (client/read-cell println-edn-string))
                (client/display-composition))
      (h/label "Add println of edn string ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value println-edn-string
               :keyup #(reset! println-edn-string @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" println-edn-string))
        :type "submit"
        "OK"))
    (h/hr)
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
          "OK")))
    (h/hr)
    ))
