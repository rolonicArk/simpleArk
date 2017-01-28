(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn do-composition
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Composition!" client/form-name))
    ))
