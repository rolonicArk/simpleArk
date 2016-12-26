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

(users/add-user! "Fred" "fred" {:welcome {:full-name "Freddy Krueger"}
                                :profile {}
                                :contacts {}
                                :console {}
                                })
(users/add-user! "Sam" "sam" {:welcome {:full-name "Sam I Am"}
                              :profile {}
                              :contacts {}
                              :console {}
                              })
(users/add-user! "Kris" "kris" {:welcome {:full-name "Kris Kringle"}
                                :profile {}})

(users/add-capability :welcome)
(users/add-capability :profile)
(users/add-capability :contacts)
(users/add-capability :console)

(ark-db/open-ark! console/ark-db)

(builder/transaction!
  console/ark-db
  {}
  (-> []
      (builder/build-je-property [:index/headline] "Build demo data")
      (demo-builds/build-demo)
      ))

(console/initializer)

(console/update-ark-record! (ark-db/get-ark-record console/ark-db))

(def handler tiples/routes)
