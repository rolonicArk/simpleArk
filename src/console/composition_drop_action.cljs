(ns console.composition-drop-action
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [cljs.reader :as reader]))

(def action-nbr (j/cell ""))

(defn valid?
  [nbr]
  (try
    (let [nbr (reader/read-string nbr)]
      (if (not (integer? nbr))
        false
        (if (> 1 nbr)
          false
          (if (> nbr (count @client/actions))
            false
            true))))
    (catch :default e
      false)))

(defn do-drop-action
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (not (empty? client/actions)))
    (h/form
      :submit (fn []
                (if (valid? @action-nbr)
                  (let [i (- (reader/read-string @action-nbr) 1)
                        actions @client/actions
                        left (subvec actions 0 i)
                        j (+ i 1)
                        right (subvec actions j)]
                    (reset! client/actions
                            (into left right))))
                  (client/display-composition))
      (h/label "Drop action# ")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value action-nbr
               :keyup #(reset! action-nbr @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (valid? action-nbr))
        :type "submit"
        "OK"))))
