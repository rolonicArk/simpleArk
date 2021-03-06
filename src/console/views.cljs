(ns console.views
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [tiples.login :as login]
    [console.commands-view :as commands-view]
    [console.forms-view :as forms-view]
    [console.history-view :as history-view]
    [console.output-view :as output-view]
    [console.client :as client]))

(defn td2-style [width]
  (str "width:" (/ width 2) "px"))

(defn tx-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (- windowInnerHeight header-height 50) "px;vertical-align:bottom")))

(defn tx2-style [windowInnerHeight header-height]
  (let [header-height (if (= header-height 0) 10 header-height)]
    (str "overflow:scroll;height:" (quot (- windowInnerHeight header-height 50) 2) "px;vertical-align:bottom")))

(def do-all
  (h/table
    :style "width:100%"
    (h/tr
      (h/td
        :style (j/cell= (td2-style login/windowInnerWidth))
        (h/div
          :style (j/cell= (tx2-style login/windowInnerHeight login/header-height))
          (commands-view/do-commands))
        (h/div
          :style (j/cell= (tx2-style login/windowInnerHeight login/header-height))
          (forms-view/do-forms)))
      (h/td
        :style (j/cell= (td2-style login/windowInnerWidth))
        (h/div
          :style (j/cell= (tx2-style login/windowInnerHeight login/header-height))
          (history-view/do-history "a"))
        (h/div
          :style (j/cell= (tx2-style login/windowInnerHeight login/header-height))
          (output-view/do-output))))))

(defmethod login/add-body-element :console [_]
  (h/div
    (h/div
      :css {:display "none"}
      :toggle (j/cell= (= 0 client/display-mode))
      (do-all))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 1 client/display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (commands-view/do-commands)))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 2 client/display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (forms-view/do-forms)))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 3 client/display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (history-view/do-history "b")))
    (h/div
      :css {:display "none" :width "100%"}
      :toggle (j/cell= (= 4 client/display-mode))
      (h/div
        :style (j/cell= (tx-style login/windowInnerHeight login/header-height))
        (output-view/do-output)))
    ))

(defmethod login/add-header-element :console [_]
  (h/div
    (h/h2 "Ark Console")
    (h/table
      :style "width:100%"
      (h/tr
        (h/td
          :style "width:16%"
          (h/span
            :style "font-weight:bold"
            "connected: ")
          (h/text client/channel-open))
        (h/td
          :style "width:16%"
          (h/button
            :click (fn []
                     (reset! client/display-mode 0)
                     (reset! client/history []))
            "clear history"))
        (h/td
          :style "font-weight:bold; width:16%; text-align: center"
          "views:")
        (h/td
          :style (j/cell= (if (= 0 client/display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! client/display-mode 0)
          (h/span "composite"))
        (h/td
          :style (j/cell= (if (= 1 client/display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! client/display-mode 1)
          (h/span "commands"))
        (h/td
          :style (j/cell= (if (= 2 client/display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! client/display-mode 2)
          (h/span "form"))
        (h/td
          :style (j/cell= (if (= 3 client/display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! client/display-mode 3)
          (h/span "history"))
        (h/td
          :style (j/cell= (if (= 4 client/display-mode)
                            "font-weight:bold; text-align:center"
                            "width:16%; color:purple; cursor:pointer; text-decoration:underline; text-align:center"))
          :click #(reset! client/display-mode 4)
          (h/span "output"))))))
