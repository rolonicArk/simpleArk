(ns console.composition-move-action
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [cljs.reader :as reader]
    [simpleArk.mapish :as mapish]))

(def action-from (j/cell ""))
(def action-to (j/cell ""))

(defn valid1
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

(defn validate-move
  [f t]
  (and
    (valid1 f)
    (valid1 t)
    (not= (reader/read-string f) (reader/read-string t))))

(defn do-move-action
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (< 1 (count client/actions)))
    (h/form
      :submit (fn []
                (if (validate-move @action-from @action-to)
                  (let [i (- (reader/read-string @action-from) 1)
                        actions @client/actions
                        a (nth actions i)
                        left (subvec actions 0 i)
                        j (+ i 1)
                        right (subvec actions j)
                        both (into left right)
                        k (- (reader/read-string @action-to) 1)
                        left (subvec both 0 k)
                        left (conj left a)
                        right (subvec both k)
                        both (into left right)]
                    (reset! client/actions both)))
                (client/display-composition))
      (h/label "Move action# ")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value action-from
               :keyup #(reset! action-from @%))
      (h/label " to position# ")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value action-to
               :keyup #(reset! action-to @%))
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (validate-move action-from action-to))
        :type "submit"
        "OK"))))
