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

(users/add-user! "Fred" {})
(users/add-user! "Sam" {})
(users/add-user! "Kris" {})

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

(defmethod users/get-common :welcome [capability-kw] {})
