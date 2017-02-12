(ns console.hello-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn hello-transaction [name]
  (builder/transaction!
    {:local/name name}
    (-> []
        (builder/build-println
          ["Hello " :local/name "!"])
        (builder/build-je-property
          [:index/headline] "Just for fun!"))))

(def aname (j/cell ""))

(defn get-aname []
  (if (= "" @aname)
    "world"
    @aname))

(defn do-hello
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Hello world!" client/form-name))
    (h/form
      :submit (fn []
                (reset! client/display-mode 0)
                (client/add-prompt!)
                (client/add-history! ">")
                (client/add-history! (str "Hello " (get-aname) "! transaction\n") client/command-prefix-style)
                (hello-transaction (get-aname)))
      (h/div
        (h/label "Hello ")
        (h/input :type "text"
                 :value aname
                 :keyup #(reset! aname @%))
        (h/label "!"))
      (h/button
        :style "background-color:MistyRose"
        :type "submit"
        "Submit"))))

