(ns demo
   (:require [tiples.server :as tiples]
             [welcome.demo]
             [profile.server]
             [contacts.demo]
             [console.demo]))

(def handler tiples/routes)
