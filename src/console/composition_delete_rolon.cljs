(ns console.composition-delete-rolon
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def delete-uuid (j/cell ""))

(defn valid
  [uuid]
  (client/valid-parameter uuid))

(defn do-delete-rolon
  []
  (h/form
    :submit (fn []
              (if (valid @delete-uuid)
                (swap! client/actions builder/build-delete-rolon
                       (keyword "local" @delete-uuid)))
              (client/output-composition!))
    (h/label "Delete Rolon :local/")
    (h/input :type "text"
             :css {:background-color "LightYellow"}
             :value delete-uuid
             :keyup #(reset! delete-uuid @%))
    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (valid delete-uuid))
      :type "submit"
      "OK")))
