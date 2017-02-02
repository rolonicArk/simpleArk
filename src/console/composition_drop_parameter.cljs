(ns console.composition-drop-parameter
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.mapish :as mapish]))

(def parameter-name (j/cell ""))

(defn valid
  [name]
  (and
    (not= "" name)
    (some?
      (get @client/local
           (keyword "local" name)))))

(defn do-drop-parameter
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not (empty? client/local)))
    (h/form
      :submit (fn []
                (if (valid @parameter-name)
                  (swap! client/local
                         dissoc (keyword "local" @parameter-name)))
                (client/display-composition))
      (h/label "Drop parameter :local/")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value parameter-name
               :keyup #(reset! parameter-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (valid parameter-name))
        :type "submit"
        "OK"))))
