(ns console.composition-alternate
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.uuid :as suuid]))

(def alternate-rolon-name (j/cell ""))

(defn do-alternate
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not= "" client/alternate-rolon))
    (h/form
      :submit (fn []
                (swap! client/local
                       assoc
                       (keyword "local" @alternate-rolon-name)
                       (suuid/create-uuid @client/alternate-rolon))
                (client/display-composition))
      (h/label "Add Alternate Rolon as parameter :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value alternate-rolon-name
               :keyup #(reset! alternate-rolon-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" alternate-rolon-name))
        :type "submit"
        "OK"))))
