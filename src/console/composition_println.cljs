(ns console.composition-println
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def println-edn-string (j/cell ""))

(defn do-println
  []
  (h/form
    :submit (fn []
              (swap! client/actions builder/build-println (client/read-cell println-edn-string))
              (client/display-composition))
    (h/label "Add println of edn string ")
    (h/input :type "text"
             :css {:background-color "PowderBlue"}
             :value println-edn-string
             :keyup #(reset! println-edn-string @%))
    (h/label " ")
    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (not= "" println-edn-string))
      :type "submit"
      "OK")))
