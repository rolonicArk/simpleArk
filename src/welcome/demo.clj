(ns welcome.demo
  (:require [tiples.server :as tiples]
            [profile.server]
            [contacts.demo]
            [console.server :as console]
            [tiples.users :as users]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.builder :as builder]
            [welcome.demo-builds :as demo-builds]
            [simpleArk.actions]))

(users/add-user! "Fred" {:welcome {:full-name "Freddy Krueger"}
                                :profile {:phone "999-555-1212" :email "fk@blood.org"}
                                :contacts {}
                                :console {}
                                })
(users/add-user! "Sam" {:welcome {:full-name "Sam I Am"}
                              :profile {:phone "123-555-6789"}
                              :contacts {}
                              :console {}
                              })
(users/add-user! "Kris" {:welcome {:full-name "Kris Kringle"}
                                :profile {}})

(users/add-capability! :welcome)
(users/add-capability! :profile)
(users/add-capability! :contacts)
(users/add-capability! :console)

(ark-db/open-ark! users/ark-db)

(builder/transaction!
  users/ark-db
  {}
  (-> []
      (builder/build-je-property [:index/headline] "Build demo data")
      (demo-builds/build-demo)
      ))

(console/initializer)

(console/update-ark-record! (ark-db/get-ark-record users/ark-db))

(def handler tiples/routes)
