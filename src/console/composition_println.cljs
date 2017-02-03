(ns console.composition-println
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(def edn-cell (j/cell ""))

(defn valid
  [edn-string]
  (try
    (and
      (not= edn-string "")
      (do
        (client/reader edn-string)
        true))
    (catch :default e
      false)))

(defn do-println
  []
  (h/form
    :submit (fn []
              (if (valid @edn-cell)
                  (swap! client/actions builder/build-println (client/read-cell edn-cell)))
              (client/display-composition))
    (h/label "Add println of edn string ")
    (h/input :type "text"
             :css {:background-color "PowderBlue"}
             :value edn-cell
             :keyup #(reset! edn-cell @%))
    (h/label " ")
    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (valid edn-cell))
      :type "submit"
      "OK")))
