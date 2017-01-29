(ns console.composition-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]
    [simpleArk.uuid :as suuid]
    [cljs.reader :as reader]))

(def composition (j/cell [{}[]]))

(def local (j/cell=
             (first composition)
             (fn [new-local]
               (swap!
                 composition
                 (fn [old-composition]
                   [new-local (second old-composition)])))))

(def actions (j/cell=
             (second composition)
             (fn [new-actions]
               (swap!
                 composition
                 (fn [old-composition]
                   [(first old-composition) new-actions])))))

(def selected-rolon-name (j/cell ""))

(def alternate-rolon-name (j/cell ""))

(def gen-uuid-name (j/cell ""))

(def println-edn-string (j/cell ""))

(def property-uuid (j/cell ""))

(def property-path (j/cell ""))

(def property-value (j/cell ""))

(defn display-composition
  []
  (reset! client/display-mode 0)
  (client/clear-output!)
  (client/add-output! "Composed Transaction")
  (client/output-tran! @client/my-ark-record @composition))

(defn do-composition
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Composition!" client/form-name))
    (h/div
      (h/button
        :css {:background-color "MistyRose"}
        :click display-composition
        "display")
      (h/button
        :css {:background-color "MistyRose"}
        :click (fn []
                 (reset! composition [{} []])
                 (display-composition))
        "reset")
      (h/button
        :css {:background-color "MistyRose"}
        :click (fn []
                 (reset! client/display-mode 0)
                 (client/add-prompt)
                 (client/add-history! ">")
                 (client/add-history! "Composition! transaction\n" client/command-prefix-style)
                 (builder/transaction! @local @actions))
        "Submit Composition! transaction")
      )
    (h/hr)
    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" client/selected-rolon))
      (h/form
        :submit (fn []
                  (swap! local
                         assoc
                         (keyword "local" @selected-rolon-name)
                         (suuid/create-uuid @client/selected-rolon))
                  (display-composition))
        (h/label "Add Selected Rolon as parameter :local/")
        (h/input :type "text"
                 :css {:background-color "LightYellow"}
                 :value selected-rolon-name
                 :keyup #(reset! selected-rolon-name @%))
        (h/label " ")
        (h/button
          :css {:display "none" :background-color "MistyRose"}
          :toggle (j/cell= (not= "" selected-rolon-name))
          :type "submit"
          "OK")))
    (h/div
      :css {:display "none"}
      :toggle (j/cell= (not= "" client/alternate-rolon))
      (h/form
        :submit (fn []
                  (swap! local
                         assoc
                         (keyword "local" @alternate-rolon-name)
                         (suuid/create-uuid @client/alternate-rolon))
                  (display-composition))
        (h/label "Add Alternate Rolon as parameter :local/")
        (h/input :type "text"
                 :css {:background-color "LightYellow"}
                 :value alternate-rolon-name
                 :keyup #(reset! alternate-rolon-name @%))
        (h/label " ")
        (h/button
          :css {:display "none" :background-color "MistyRose"}
          :toggle (j/cell= (not= "" alternate-rolon-name))
          :type "submit"
          "OK")))
    (h/hr)
    (h/form
      :submit (fn []
                (swap! actions builder/build-gen-uuid @gen-uuid-name)
                (display-composition))
      (h/label "Add gen-uuid to :local/")
      (h/input :type "text"
               :css {:background-color "LightYellow"}
               :value gen-uuid-name
               :keyup #(reset! gen-uuid-name @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" gen-uuid-name))
        :type "submit"
        "OK"))
    (h/form
      :submit (fn []
                (swap! actions builder/build-println (reader/read-string @println-edn-string))
                (display-composition))
      (h/label "Add println of edn string ")
      (h/input :type "text"
               :css {:background-color "PowderBlue"}
               :value println-edn-string
               :keyup #(reset! println-edn-string @%))
      (h/label " ")
      (h/button
        :css {:display "none" :background-color "MistyRose"}
        :toggle (j/cell= (not= "" println-edn-string))
        :type "submit"
        "OK"))
    (h/hr)
    (h/form
      :submit (fn []
                (swap! actions builder/build-property
                       (reader/read-string @property-uuid)
                       (reader/read-string @property-path)
                       (reader/read-string @property-value))
                (display-composition))
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
                 :keyup #(reset! property-value @%)))
      (h/div
        (h/button
          :css {:display "none" :background-color "MistyRose"}
          :toggle (j/cell= (and
                             (not= "" property-uuid)
                             (not= "" property-path)
                             (not= "" property-value)
                             ))
          :type "submit"
          "OK")))
    (h/hr)
    ))
