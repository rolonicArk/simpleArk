(ns console.views
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [tiples.login :as login]
    [console.client :as client]))

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
