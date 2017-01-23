(ns console.forms-view
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]))

(defn do-forms []
  (h/div
    (h/div
      :style "font-weight:bold"
      (h/text
        (str "Form: " client/form-name)))


    ))
