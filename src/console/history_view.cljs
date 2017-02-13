(ns console.history-view
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]))

(defn do-history
  [id-prefix]
  (h/div :style "white-space:pre-wrap; font-family:monospace"
         (h/for-tpl [[txt style on-click arg txt-id] client/history]
                    (h/output
                      :id (j/cell= (str id-prefix txt-id))
                      :style style
                      :click (fn [] (@on-click @client/my-ark-record @arg))
                      txt))))
