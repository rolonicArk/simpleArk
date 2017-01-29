(ns console.composition-selected
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.uuid :as suuid]))

(def selected-rolon-name (j/cell ""))

(defn do-selected
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not= "" client/selected-rolon))
    (h/form
      :submit (fn []
                (swap! client/local
                       assoc
                       (keyword "local" @selected-rolon-name)
                       (suuid/create-uuid @client/selected-rolon))
                (client/display-composition))
      (h/label "Add Selected Rolon as parameter :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value selected-rolon-name
               :keyup #(reset! selected-rolon-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" selected-rolon-name))
        :type "submit"
        "OK"))))
