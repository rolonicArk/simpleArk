(ns demo
  (:require [tiples.server :as tiples]
            [welcome.demo]
            [profile.server]
            [contacts.demo]
            [console.demo :as console]
            [tiples.users :as users]))

(users/add-capability :welcome)
(users/add-capability :profile)
(users/add-capability :contacts)
(users/add-capability :console)

(console/initializer)

(def handler tiples/routes)
