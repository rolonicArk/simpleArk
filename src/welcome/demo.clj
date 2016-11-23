(ns welcome.demo
  (:require [tiples.server :as tiples]
            [profile.server]
            [contacts.demo]
            [console.demo :as console]
            [tiples.users :as users]))

(users/add-user! "Fred" "fred" {:welcome {:full-name "Freddy Krueger"}
                                :profile {}
                                :contacts {}
                                :console {}})
(users/add-user! "Sam" "sam" {:welcome {:full-name "Sam I Am"}
                              :profile {}
                              :contacts {}})
(users/add-user! "Kris" "kris" {:welcome {:full-name "Kris Kringle"}
                                :profile {}})

(users/add-capability :welcome)
(users/add-capability :profile)
(users/add-capability :contacts)
(users/add-capability :console)

(console/initializer)

(def handler tiples/routes)
