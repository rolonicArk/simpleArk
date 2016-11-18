(ns demo
   (:require [tiples.server :as tiples]
             [welcome.demo]
             [profile.server]
             [contacts.demo]
             [console.demo :as console]
             ))

(console/initializer)

(def handler tiples/routes)
