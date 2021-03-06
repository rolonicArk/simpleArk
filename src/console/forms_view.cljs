(ns console.forms-view
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.hello-transaction :as hello]
    [console.make-transaction :as make]
    [console.trouble-transaction :as trouble]
    [console.invalid-transaction :as invalid]
    [console.composition-transaction :as composition]
    [console.client :as client]))

(defn do-forms []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (and
                       client/channel-open
                       (= "" client/selected-time)))
    (h/div
      :style "color:red"
      (h/p (h/text (if client/transaction-error
                     (str "Error: " client/transaction-error-msg)
                     ""))))

    (h/div
      :style "font-weight:bold"
      (h/text
        (str "Form: " client/form-name)))

    (hello/do-hello)
    (make/do-make)
    (trouble/do-trouble)
    (invalid/do-invalid)
    (composition/do-composition)
    ))
