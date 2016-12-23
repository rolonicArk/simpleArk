(ns console.demo
  (:require [console.server :as console]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.uuid :as suuid]))

(defmethod ark-value/eval-transaction :trouble!
  [ark-value ark-db n s]
  (println "throwing exception")
  (throw (new IllegalArgumentException "A troublesome transaction")))

(defmethod ark-value/eval-transaction :hello-world!
  [ark-value ark-db n s]
  (println "Hello," s)
  (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-value)]
    (ark-value/update-property ark-value ark-db je-uuid [:index/headline] "Just for fun!")))

(def ark-db console/ark-db)

(def welcome-uuid (suuid/random-uuid ark-db))
(def profile-uuid (suuid/random-uuid ark-db))
(def contacts-uuid (suuid/random-uuid ark-db))
(def console-uuid (suuid/random-uuid ark-db))

(def fred-uuid (suuid/random-uuid ark-db))
(def sam-uuid (suuid/random-uuid ark-db))
(def kris-uuid (suuid/random-uuid ark-db))

(def welcome-fred-uuid (suuid/random-uuid ark-db))
(def welcome-sam-uuid (suuid/random-uuid ark-db))
(def welcome-kris-uuid (suuid/random-uuid ark-db))

(def profile-fred-uuid (suuid/random-uuid ark-db))
(def profile-sam-uuid (suuid/random-uuid ark-db))
(def profile-kris-uuid (suuid/random-uuid ark-db))

(def contacts-fred-uuid (suuid/random-uuid ark-db))
(def contacts-sam-uuid (suuid/random-uuid ark-db))

(def console-fred-uuid (suuid/random-uuid ark-db))
(def console-sam-uuid (suuid/random-uuid ark-db))

(defn initializer
  []
  (console/initializer)
  (ark-db/open-ark! ark-db)

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [welcome-uuid
       {[:index/headline] "create welcome capability"}
       {[:index/headline] "Welcome Capability"
        [:index/name] "welcome-capability"
        [:index/capability.name] "welcome"
        [:rel/capability.use welcome-fred-uuid] true
        [:rel/capability.use welcome-sam-uuid] true
        [:rel/capability.use welcome-kris-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [profile-uuid
       {[:index/headline] "create profile capability"}
       {[:index/headline] "Profile Capability"
        [:index/name] "profile-capability"
        [:index/capability.name] "profile"
        [:rel/capability.use profile-fred-uuid] true
        [:rel/capability.use profile-sam-uuid] true
        [:rel/capability.use profile-kris-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [contacts-uuid
       {[:index/headline] "create contacts capability"}
       {[:index/headline] "Contacts Capability"
        [:index/name] "contacts-capability"
        [:index/capability.name] "contacts"
        [:rel/capability.use contacts-fred-uuid] true
        [:rel/capability.use contacts-sam-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [console-uuid
       {[:index/headline] "create console capability"}
       {[:index/headline] "Console Capability"
        [:index/name] "console-capability"
        [:index/capability.name] "console"
        [:rel/capability.use console-fred-uuid] true
        [:rel/capability.use console-sam-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [fred-uuid
       {[:index/headline] "create user Fred"}
       {[:index/name] "user-Fred"
        [:index/user.name] "Fred"
        [:content/password] "fred"
        [:rel/user.capability welcome-fred-uuid] true
        [:rel/user.capability profile-fred-uuid] true
        [:rel/user.capability contacts-fred-uuid] true
        [:rel/user.capability console-fred-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [welcome-fred-uuid
       {[:index/headline] "create welcome-Fred"}
       {[:index/name] "welcome-Fred"
        [:inv-rel/capability.use welcome-uuid] true
        [:inv-rel/user.capability fred-uuid] true
        [:content/full-name] "Freddy Krueger"}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [profile-fred-uuid
       {[:index/headline] "create profile-Fred"}
       {[:index/name] "profile-Fred"
        [:inv-rel/capability.use profile-uuid] true
        [:inv-rel/user.capability fred-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [contacts-fred-uuid
       {[:index/headline] "create contacts-Fred"}
       {[:index/name] "contacts-Fred"
        [:inv-rel/capability.use contacts-uuid] true
        [:inv-rel/user.capability fred-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [console-fred-uuid
       {[:index/headline] "create console-Fred"}
       {[:index/name] "console-Fred"
        [:inv-rel/capability.use console-uuid] true
        [:inv-rel/user.capability fred-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [sam-uuid
       {[:index/headline] "create user Sam"}
       {[:index/name] "user-Sam"
        [:index/user.name] "Sam"
        [:content/password] "sam"
        [:rel/user.capability welcome-sam-uuid] true
        [:rel/user.capability profile-sam-uuid] true
        [:rel/user.capability contacts-sam-uuid] true
        [:rel/user.capability console-sam-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [welcome-sam-uuid
       {[:index/headline] "create welcome-Sam"}
       {[:index/name] "welcome-Sam"
        [:inv-rel/capability.use welcome-uuid] true
        [:inv-rel/user.capability sam-uuid] true
        [:content/full-name] "Sam I Am"}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [profile-sam-uuid
       {[:index/headline] "create profile-Sam"}
       {[:index/name] "profile-Sam"
        [:inv-rel/capability.use profile-uuid] true
        [:inv-rel/user.capability sam-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [contacts-sam-uuid
       {[:index/headline] "create contacts-Sam"}
       {[:index/name] "contacts-Sam"
        [:inv-rel/capability.use contacts-uuid] true
        [:inv-rel/user.capability sam-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [console-sam-uuid
       {[:index/headline] "create console-Sam"}
       {[:index/name] "console-Sam"
        [:inv-rel/capability.use console-uuid] true
        [:inv-rel/user.capability sam-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [kris-uuid
       {[:index/headline] "create user Kris"}
       {[:index/name] "user-Kris"
        [:index/user.name] "Kris"
        [:content/password] "kris"
        [:rel/user.capability welcome-kris-uuid] true
        [:rel/user.capability profile-kris-uuid] true}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [welcome-kris-uuid
       {[:index/headline] "create welcome-Kris"}
       {[:index/name] "welcome-Kris"
        [:inv-rel/capability.use welcome-uuid] true
        [:inv-rel/user.capability kris-uuid] true
        [:content/full-name] "Kris Kringle"}]))

  (ark-db/process-transaction!
    ark-db
    :ark/update-rolon-transaction!
    (prn-str
      [profile-kris-uuid
       {[:index/headline] "create profile-Kris"}
       {[:index/name] "profile-Kris"
        [:inv-rel/capability.use profile-uuid] true
        [:inv-rel/user.capability kris-uuid] true}]))

  (console/update-ark-record! (ark-db/get-ark-record ark-db))
  )
