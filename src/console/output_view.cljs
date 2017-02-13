(ns console.output-view
  (:require
    [hoplon.core :as h]
    [console.client :as client]))

(defn do-output
  []
  (h/div
    :style "white-space:pre-wrap; font-family:monospace"
    (h/for-tpl [[txt style on-click arg txt-id] client/output]
               (h/output
                 :id txt-id
                 :style style
                 :click (fn [] (@on-click @client/my-ark-record @arg))
                 txt))))
