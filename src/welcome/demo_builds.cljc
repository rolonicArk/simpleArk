(ns welcome.demo-builds
  (:require
    [simpleArk.builder :as builder]
    ))

#?(:clj
   (set! *warn-on-reflection* true))

(defn build-capability
  [actions capability]
  (conj actions [:capability capability]))

(defn update-contact
  [actions contact value]
  (conj actions [:contact contact value]))

(defn build-user
  [actions user password]
  (conj actions [:user user password]))

(defn build-user-capability
  [actions user capability]
  (conj actions [:user-capability user capability]))

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
