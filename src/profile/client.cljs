(ns profile.client
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [tiples.client :as tiples]
    [tiples.login :as login]))

(j/defc my-profile nil)

(defn watch-user-data [k r o n]
  (reset! my-profile
          (if n
            (:profile n)
            nil)))

(add-watch login/user-data :profile watch-user-data)

(j/defc= my-phone
         (if my-profile
           (get my-profile :phone "")
           nil)
         (partial swap! my-profile assoc :phone))

(j/defc= my-email
         (if my-profile
           (get my-profile :email "")
           nil)
         (partial swap! my-profile assoc :email))

(defn submit-user-data []
  (swap! login/user-data assoc :profile @my-profile)
  (tiples/chsk-send! [:profile/update @my-profile]))

(defn reset-user-data []
  (reset! my-profile (:profile @login/user-data)))

(defn disabled? []
  (j/cell= (= my-profile (:profile login/user-data))))

(def do-profile
  (h/div
    (h/div :id "header"
           :style "background-color:#f0fff0"
           (login/tabs-div)
           (h/h2 "Profile"))
    (h/form
      :submit #(submit-user-data)
      (h/table
        (h/tr
          (h/td (h/label "Phone "))
          (h/td (h/input :type "text"
                         :value my-phone
                         :keyup #(reset! my-phone @%))))
        (h/tr
          (h/td (h/label "Email "))
          (h/td (h/input :type "text"
                         :value my-email
                         :keyup #(reset! my-email @%))))
        (h/tr
          (h/td (h/button :type "submit"
                          :disabled (disabled?)
                          "submit"))
          (h/td :style "text-align:right" (h/button :click #(reset-user-data)
                                                    :disabled (disabled?)
                                                    "reset"))
          )
        )
      )))

(defmethod login/add-element :profile [_]
  (do-profile))
