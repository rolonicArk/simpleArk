(ns welcome.demo-builds
  (:require
    [simpleArk.builder :as builder]
    ))

#?(:clj
   (set! *warn-on-reflection* true))

#_(defn build-capability
  [actions capability]
  (let [capability-kw (keyword "local" (str capability "-capability"))]
    (-> actions
        (builder/build-gen-uuid capability-kw)
        (builder/build-property capability-kw [:index/name] (str capability "-capability"))
        (builder/build-property capability-kw [:index/capability-name] capability)
        (builder/build-property capability-kw [:index/headline] (str  "capability " capability)))))

(defn build-capability
  [actions capability]
  (conj actions [:capability capability]))

(defn update-contact
  [actions contact value]
  (builder/build-property actions
                          :local/contacts-capability
                          (into [:content/contact] (into (sorted-map) contact))
                          value))

(defn build-user
  [actions user password]
  (let [user-kw (keyword "local" (str user "-user"))]
    (-> actions
        (builder/build-gen-uuid user-kw)
        (builder/build-property user-kw [:index/name] (str user "-user"))
        (builder/build-property user-kw [:index/user-name] user)
        (builder/build-property user-kw [:index/headline] (str  "user " user))
        (builder/build-property user-kw [:content/password] password))))

(defn build-user-capability
  [actions user capability]
  (let [user-capability-kw (keyword "local" (str user "-" capability))
        user-kw (keyword "local" (str user "-user"))
        capability-kw (keyword "local" (str capability "-capability"))]
    (-> actions
        (builder/build-gen-uuid user-capability-kw)
        (builder/build-property user-capability-kw [:index/name] (str user "-" capability))
        (builder/build-property user-capability-kw [:index/headline] (str user " " capability))
        (builder/build-relation :rel/capability user user-capability-kw capability-kw)
        (builder/build-relation :rel/user (keyword capability) user-capability-kw user-kw)
        )))

(defn build-demo
  [actions]
  (-> actions
      (build-capability "welcome")
      (build-capability "profile")
      (build-capability "contacts")
      (update-contact {:first "Ben" :last "Bitdiddle" :email "benb@mit.edu"} true)
      (update-contact {:first "Alyssa" :middle-initial "P" :last "Hacker" :email "aphacker@mit.edu"} true)
      (update-contact {:first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"} true)
      (update-contact {:first "Louis" :last "Reasoner" :email "prolog@mit.edu"} true)
      (update-contact {:first "Cy" :middle-initial "D" :last "Effect" :email "bugs@mit.edu"} true)
      (update-contact {:first "Lem" :middle-initial "E" :last "Tweakit" :email "morebugs@mit.edu"} true)
      (build-capability "console")
      (build-user "Fred" "fred")
      (build-user-capability "Fred" "welcome")
      (builder/build-property :local/Fred-welcome [:content/data :full-name] "Freddy Krueger")
      (builder/build-relation :rel/watches :welcome :local/welcome-capability :local/Fred-welcome)
      (build-user-capability "Fred" "profile")
      (builder/build-property :local/Fred-profile [:content/data :phone] "999-555-1212")
      (builder/build-property :local/Fred-profile [:content/data :email] "fk@blood.org")
      (build-user-capability "Fred" "contacts")
      (build-user-capability "Fred" "console")
      (build-user "Sam" "sam")
      (build-user-capability "Sam" "welcome")
      (builder/build-property :local/Sam-welcome [:content/data :full-name] "Sam I Am")
      (builder/build-relation :rel/watches :welcome :local/welcome-capability :local/Sam-welcome)
      (build-user-capability "Sam" "profile")
      (builder/build-property :local/Sam-profile [:content/data :phone] "333-555-9876")
      (build-user-capability "Sam" "contacts")
      (build-user-capability "Sam" "console")
      (build-user "Kris" "kris")
      (build-user-capability "Kris" "welcome")
      (builder/build-property :local/Kris-welcome [:content/data :full-name] "Kris Kringle")
      (builder/build-relation :rel/watches :welcome :local/welcome-capability :local/Kris-welcome)
      (build-user-capability "Kris" "profile")
      ))
