(ns welcome.client
  (:require
    [hoplon.core :as h]
    [tiples.login :as login]))

(defmethod login/add-header-element :welcome [_]
  (h/h2 (h/text "Hello ~{(:full-name (:welcome login/user-data))}.")))

(defmethod login/add-body-element :welcome [_]
  (h/div))
