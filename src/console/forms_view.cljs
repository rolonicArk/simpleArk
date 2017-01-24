(ns console.forms-view
  (:require
    [hoplon.core :as h]
    [console.trouble-transaction :as trouble]
    [console.invalid-transaction :as invalid]
    [console.client :as client]))

(defn do-forms []
  (h/div
    (h/div
      :style "color:red"
      (h/p (h/text (if client/transaction-error
                     (str "Error: " client/transaction-error-msg)
                     ""))))

    (h/div
      :style "font-weight:bold"
      (h/text
        (str "Form: " client/form-name)))

    (trouble/do-trouble)
    (invalid/do-invalid)
    ))
