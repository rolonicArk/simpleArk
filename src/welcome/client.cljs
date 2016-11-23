(ns welcome.client
  (:require
    [hoplon.core :as h]
    [tiples.login :as login]))

(def do-welcome
  (h/div :id "header"
         :style "background-color:#f8f8f0"
         (login/tabs-div)
         (h/h2 (h/text "Hello ~{(:full-name (:welcome login/user-data))}."))))

(defmethod login/add-element :welcome [_]
  (do-welcome))
