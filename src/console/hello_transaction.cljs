(ns console.hello-transaction
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.builder :as builder]))

(defn hello-transaction [name]
  (builder/transaction!
    {}
    (-> []
        (builder/build-println
          (str "Hello " name "!"))
        (builder/build-je-property
          [:index/headline] "Just for fun!"))))

(defn do-hello
  []
  (h/div
    :css {:display "none"}
    :toggle (j/cell= (= "Hello world!" client/form-name))

    (h/button
      :style "background-color:MistyRose"
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "Hello Fred transaction\n" client/command-prefix-style)
               (hello-transaction "Fred"))
      "Submit")))

