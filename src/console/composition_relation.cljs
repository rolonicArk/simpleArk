(ns console.composition-relation
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]
    [simpleArk.mapish :as mapish]))

(def relation-kw (j/cell ""))
(def relation-label (j/cell ""))
(def relation-from (j/cell ""))
(def relation-to (j/cell ""))
(def relation-value (j/cell ""))

(defn valid1
  [edn-string]
  (try
    (and
      (not= edn-string "")
      (do
        (client/reader edn-string)
        true))
    (catch :default e
      false)))

(defn validate-kw
  [kw]
  (or
    (mapish/bi-rel? kw)
    (mapish/rel? kw)
    (mapish/inv-rel? kw)))

(defn valid
  [kw label f t value]
  (and
    (valid1 kw)
    (client/error (not (validate-kw (client/reader kw)))
                  "Not a valid relation keyword")
    (valid1 label)
    (or (client/valid-parameter f)(= f "je"))
    (or (client/valid-parameter t)(= t "je"))
    (valid1 value)))

(defn do-relation
  []
  (h/form
    :submit (fn []
              (if (valid @relation-kw @relation-label @relation-from @relation-to @relation-value)
                (swap! client/actions builder/build-relation
                       (client/read-cell relation-kw)
                       (client/read-cell relation-label)
                       (keyword "local" @relation-from)
                       (keyword "local" @relation-to)
                       (client/read-cell relation-value)))
              (client/display-composition))
    (h/label "Add a relation")
    (h/div
      (h/label "Relation keyword: ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value relation-kw
               :keyup #(reset! relation-kw @%)))
    (h/div
      (h/label "Label: ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value relation-label
               :keyup #(reset! relation-label @%))
      (h/label " (nil -> no label)"))
    (h/div
      (h/label "From: :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value relation-from
               :keyup #(reset! relation-from @%))
      (h/label " (je -> transaction journal entry)"))
    (h/div
      (h/label "To: :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value relation-to
               :keyup #(reset! relation-to @%))
      (h/label " (je -> transaction journal entry)"))
    (h/div
      (h/label "Value: ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value relation-value
               :keyup #(reset! relation-value @%))
      (h/label " (nil -> remove) ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (valid relation-kw relation-label relation-from relation-to relation-value))
        :type "submit"
        "OK"))))
