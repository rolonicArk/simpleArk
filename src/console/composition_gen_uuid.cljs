(ns console.composition-gen-uuid
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def action-name (j/cell ""))

(defn do-gen-uuid
  []
  (h/form
    :submit (fn []
              (if (client/valid-parameter @action-name)
                (swap! client/actions builder/build-gen-uuid (keyword "local" @action-name)))
              (client/display-composition))
    (h/label "Add gen-uuid to :local/")
    (h/input :type "text"
             :css {:background-color "LightYellow"}
             :value action-name
             :keyup #(reset! action-name @%))
    (h/label " ")
    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (client/valid-parameter action-name))
      :type "submit"
      "OK")))
