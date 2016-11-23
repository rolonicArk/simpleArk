(ns ^{:hoplon/page "index.html"} welcome.index
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [clojure.string :as string]
    [tiples.client :as tiples]
    [tiples.login :as login]
    [welcome.client]
    [profile.client]
    [contacts.client]
    [console.client]
    ))

(defn opening
  []
  (reset! login/started true))

(tiples/on-open opening)

(tiples/start!)

(h/html
  (h/head
    (h/link :href "main.css" :rel "stylesheet"))
  (h/body
    (h/div
      :css {:display "none"}
      :toggle login/show-app?
      (h/div (h/for-tpl [capability login/capabilities]
                        (h/div
                          :css {:display "none"}
                          :toggle (j/cell= (and (some? capability) (= login/reroute (name capability))))
                          (login/add-element @capability)))))

    (login/login-div)))
