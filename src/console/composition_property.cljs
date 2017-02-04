(ns console.composition-property
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]
    [simpleArk.mapish :as mapish]))

(def property-uuid (j/cell ""))
(def property-path (j/cell ""))
(def property-value (j/cell ""))

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

(defn validate-path
  [v]
  (if (not (client/error (not (vector? v)) "path is not a vector"))
    false
    (try
      (mapish/validate-property-path v)
      (client/clear-error)
      true
      (catch :default e
        (client/set-error (str e))
        false)))
  (client/error (not (or (mapish/index? (first v))
                         (mapish/content? (first v))))
                "not an index or content"))

  (defn valid
    [uuid path value]
    (and
      (valid1 uuid)
      (valid1 path)
      (validate-path (client/reader path))
      (valid1 value)))

  (defn do-property
    []
    (h/form
      :submit (fn []
                (if (valid @property-uuid @property-path @property-value)
                  (swap! client/actions builder/build-property
                         (client/read-cell property-uuid)
                         (client/read-cell property-path)
                         (client/read-cell property-value)))
                (client/display-composition))
      (h/label "Add a property")
      (h/div
        (h/label "Rolon: ")
        (h/input :type "text"
                 :css {:background-color "PowderBlue"}
                 :value property-uuid
                 :keyup #(reset! property-uuid @%)))
      (h/div
        (h/label "Path: ")
        (h/input :type "text"
                 :css {:background-color "PowderBlue"}
                 :value property-path
                 :keyup #(reset! property-path @%)))
      (h/div
        (h/label "Value: ")
        (h/input :type "text"
                 :css {:background-color "PowderBlue"}
                 :value property-value
                 :keyup #(reset! property-value @%))
        (h/label " (nil -> remove) ")
        (h/button
          :css {:display "none" :background-color "MistyRose"}
          :toggle (j/cell= (valid property-uuid property-path property-value))
          :type "submit"
          "OK"))))
