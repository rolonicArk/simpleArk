(ns console.demo-builds
  (:require
    [simpleArk.builder :as builder]
    ))

#?(:clj
   (set! *warn-on-reflection* true))

(defn build-capability
  [actions capability]
  (let [capability-kw (keyword "local" (str capability "-capability"))]
    (-> actions
        (builder/build-gen-uuid capability-kw)
        (builder/build-property capability-kw [:index/name] (str capability "-capability"))
        (builder/build-property capability-kw [:index/capability-name] capability)
        (builder/build-property capability-kw [:index/headline] (str  "capability " capability)))))

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
        (builder/build-relation :rel/capability user-capability-kw capability-kw)
        (builder/build-relation :rel/user user-capability-kw user-kw)
        )))

(defn build-demo
  [actions]
  (-> actions
      (build-capability "welcome")
      (build-capability "profile")
      (build-capability "contacts")
      (build-capability "console")
      (build-user "Fred" "fred")
      (build-user-capability "Fred" "welcome")
      (build-user-capability "Fred" "profile")
      (build-user-capability "Fred" "contacts")
      (build-user-capability "Fred" "console")
      (build-user "Sam" "sam")
      (build-user-capability "Sam" "welcome")
      (build-user-capability "Sam" "profile")
      (build-user-capability "Sam" "contacts")
      (build-user-capability "Sam" "console")
      (build-user "Kris" "kris")
      (build-user-capability "Kris" "welcome")
      (build-user-capability "Kris" "profile")
      ))
