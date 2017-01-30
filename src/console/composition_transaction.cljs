(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [console.composition-basics :as composition-basics]
    [console.composition-selected :as composition-selected]
    [console.composition-alternate :as composition-alternate]
    [console.composition-drop-parameter :as composition-drop-parameter]
    [console.composition-gen-uuid :as composition-gen-uuid]
    [console.composition-println :as composition-println]
    [console.composition-property :as composition-property]))

(defn resets
  []
  (reset! client/composition [{} []])
  (reset! composition-selected/selected-rolon-name "")
  (reset! composition-alternate/alternate-rolon-name "")
  (reset! composition-drop-parameter/parameter-name "")
  (reset! composition-gen-uuid/gen-uuid-name "")
  (reset! composition-println/println-edn-string "")
  (reset! composition-property/property-uuid "")
  (reset! composition-property/property-path "")
  (reset! composition-property/property-value "")
  )

(defn do-composition
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Composition!" client/form-name))
    (composition-basics/do-basics resets)
    (h/hr)
    (composition-selected/do-selected)
    (composition-alternate/do-alternate)
    (console.composition-drop-parameter/do-drop-parameter)
    (h/hr)
    (composition-gen-uuid/do-gen-uuid)
    (composition-println/do-println)
    (h/hr)
    (composition-property/do-property)
    (h/hr)
    ))
