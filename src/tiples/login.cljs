(ns tiples.login
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [tiples.client :as tiples]))

(defmulti add-header-element identity)
(defmulti add-body-element identity)

(j/defc started false)
(j/defc capabilities nil)
(j/defc common-data nil)
(j/defc user-data nil)
(j/defc user-name "")
(def error (j/cell false))
(def route (h/route-cell "#/home/"))

(j/defc= capability-names (reduce
                          (fn [v i]
                            (conj v (name i)))
                          []
                          capabilities))

(defn redo-route [cn r]
  (let [r (if r
            (subs r 1)
            "")]
    (if (some (fn [x] (= x r)) cn)
      r
      (if (empty? cn) nil (cn 0)))))
(j/defc= reroute (redo-route capability-names route))

(j/defc= show-login? (and started (not user-data)))
(j/defc= show-app? (and started user-data))

(def login! (fn [user pass1]
              (reset! error false)
              (reset! user-name user)
              (tiples/chsk-send! [:users/login {:name user :password pass1}])))

(defn logout! []
  (tiples/chsk-send! [:users/logout nil]))

(defmethod tiples/chsk-recv :users/login-error
  [id ?data]
  (reset! error true))

(defn getFullScreenElement [] (or (aget js.document "fullscreenElement")
                                  (aget js.document "mozFullScreenElement")
                                  (aget js.document "webkitFullscreenElement")))

(def fullScreenElement (j/cell (getFullScreenElement)))
(def windowInnerWidth (j/cell window.innerWidth))
(def windowInnerHeight (j/cell window.innerHeight))

(defn resize []
  (.log js/console (pr-str "bodyWidth "
                           (.-width (.getBoundingClientRect (.-body js/document)))))
  (.log js/console (pr-str "bodyHeight "
                           (.-height (.getBoundingClientRect (.-body js/document)))))
  (reset! fullScreenElement (getFullScreenElement))
  (reset! windowInnerWidth window.innerWidth)
  (reset! windowInnerHeight window.innerHeight)
  (.log js/console (pr-str "windowInnerHeight " @windowInnerHeight))
  )

(set! (.-onresize js/window) resize)

(j/defc all-logged-in false)

(defmethod tiples/chsk-recv :users/logged-in
  [id ?data]
  (if (nil? ?data)
    (j/dosync
      (reset! capabilities nil)
      (reset! common-data nil)
      (reset! user-data nil))
    (do
      (j/dosync
        (reset! capabilities (?data 0))
        (reset! common-data (?data 1))
        (reset! user-data (?data 2)))
      (reset! all-logged-in true)
      (let [e (.getElementById js/document "consoleheader")]
        (.log js/console "blipper" (pr-str e)))
      )))

(defn tabs-div []
  (h/div
    (h/table (h/tr
             (h/for-tpl [capability capability-names]
                      (h/td (h/if-tpl (j/cell= (= capability reroute))
                                  (h/strong capability)
                                  (h/a :href (j/cell= (str "/#" capability)) capability))
                          " | "))
             (h/td (h/form
                   :submit #(logout!)
                   (h/button :type "submit" "log off"))))))
  )

(defn login-div []
  (h/div
    (h/div
      :id "login-pane"
      :css {:display "none"}
      :toggle show-login?
      (h/h2 :style "background-color:#f0f8f8" "Welcome to Tiples")
      (h/div
        :slide-toggle error
        :css {:display "none"}
        (h/p :style "color:red" "login error")
        )
      (let [user (j/cell "")
            pass (j/cell "")
            pass2 (j/cell "")]
        (h/form
          :submit #(login! @user @pass)
          (h/table
            (h/tr
              (h/td (h/label "Username "))
              (h/td (h/input :type "text"
                         :autofocus "autofocus"
                         :value user
                         :change #(reset! user @%))))
            (h/tr
              (h/td (h/label "Password "))
              (h/td (h/input :type "password"
                         :value pass
                         :change #(reset! pass @%))))
            (h/tr
              (h/td "")
              (h/td :style "text-align:right" (h/button :type "submit" "login"))
              )
            )
          )
        )
      )
    )
  )
