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
      (h/div :id "header"
             :style "background-color:#f8f8f0"
             (h/div
               (h/table (h/tr
                          (h/for-tpl [capability login/capability-names]
                                     (h/td (h/if-tpl (j/cell= (= capability login/reroute))
                                                     (h/strong capability)
                                                     (h/a :href (j/cell= (str "/#" capability)) capability))
                                           " | "))
                          (h/td (h/form
                                  :submit #(login/logout!)
                                  (h/button :type "submit" "log off"))))))
             (h/div
               (h/for-tpl [capability login/capabilities]
                          (h/div
                            :css {:display "none"}
                            :toggle (j/cell= (and (some? capability) (= login/reroute (name capability))))
                            (login/add-header-element @capability))))
             )
      (h/div
        (h/for-tpl [capability login/capabilities]
                   (h/div
                     :css {:display "none"}
                     :toggle (j/cell= (and (some? capability) (= login/reroute (name capability))))
                     (login/add-body-element @capability)))
        ))

    (login/login-div)))
