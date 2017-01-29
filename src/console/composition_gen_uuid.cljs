(ns console.composition-gen-uuid
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def gen-uuid-name (j/cell ""))

(defn do-gen-uuid
  []
  (h/form
    :submit (fn []
              (swap! client/actions builder/build-gen-uuid @gen-uuid-name)
              (client/display-composition))
    (h/label "Add gen-uuid to :local/")
    (h/input :type "text"
             :css {:background-color "LightYellow"}
             :value gen-uuid-name
             :keyup #(reset! gen-uuid-name @%))
    (h/label " ")
    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (not= "" gen-uuid-name))
      :type "submit"
      "OK")))
