(ns console.composition-selected
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.uuid :as suuid]))

(def parameter-name (j/cell ""))

(defn do-selected
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not= "" client/selected-rolon))
    (h/form
      :submit (fn []
                (if (client/valid-parameter @parameter-name)
                  (swap! client/local
                         assoc
                         (keyword "local" @parameter-name)
                         (suuid/create-uuid @client/selected-rolon)))
                (client/display-composition))
      (h/label (h/text (str "Add selected Rolon " (client/pretty-value client/my-ark-record (suuid/create-uuid client/selected-rolon)) " as parameter :local/")))
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value parameter-name
               :keyup #(reset! parameter-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (client/valid-parameter parameter-name))
        :type "submit"
        "OK"))))
