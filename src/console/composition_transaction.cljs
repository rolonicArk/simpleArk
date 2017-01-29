(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]
    [console.composition-basics :as composition-basics]
    [console.composition-selected :as composition-selected]
    [console.composition-alternate :as composition-alternate]
    [console.composition-gen-uuid :as composition-gen-uuid]
    [console.composition-println :as composition-println]
    [console.composition-property :as composition-property]))

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
    (composition-gen-uuid/do-gen-uuid)
    (composition-println/do-println)
    (h/hr)
    (composition-property/do-property)
    (h/hr)
    ))
