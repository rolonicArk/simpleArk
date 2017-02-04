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
    [console.composition-property :as composition-property]
    [console.composition-relation :as composition-relation]
    [console.composition-drop-action :as composition-drop-action]
    [console.composition-move-action :as composition-move-action]))

(defn resets
  []
  (reset! client/composition [{} []])
  (reset! composition-selected/parameter-name "")
  (reset! composition-alternate/parameter-name "")
  (reset! composition-drop-parameter/parameter-name "")
  (reset! composition-gen-uuid/action-name "")
  (reset! composition-println/edn-cell "")
  (reset! composition-property/property-uuid "")
  (reset! composition-property/property-path "")
  (reset! composition-property/property-value "")
  (reset! composition-drop-action/action-nbr "")
  (reset! composition-move-action/action-from "")
  (reset! composition-move-action/action-to "")
  (reset! composition-relation/relation-kw "")
  (reset! composition-relation/relation-label "")
  (reset! composition-relation/relation-from "")
  (reset! composition-relation/relation-to "")
  (reset! composition-relation/relation-value ""))

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
    (composition-relation/do-relation)
    (h/hr)
    (composition-drop-action/do-drop-action)
    (composition-move-action/do-move-action)
    ))
