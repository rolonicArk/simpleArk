(ns console.composition-drop-parameter
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]))

(def parameter-name (j/cell ""))

(defn do-drop-parameter
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not (empty? client/local)))
    (h/form
      :submit (fn []
                (swap! client/local
                       dissoc (keyword "local" @parameter-name))
                (client/display-composition))
      (h/label "Drop parameter :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value parameter-name
               :keyup #(reset! parameter-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (if (not= "" parameter-name)
                           (some?
                             (get client/local
                                  (keyword "local" parameter-name)))))
        :type "submit"
        "OK"))))
