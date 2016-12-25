(ns console.demo-builds
  (:require
    [simpleArk.builder :as builder]
    ))

#?(:clj
   (set! *warn-on-reflection* true))

(defn build-capability
  [actions capability]
  (let [uuid (keyword "local" (str capability "-capability-uuid"))]
    (-> actions
        (builder/build-gen-uuid uuid)
        (builder/build-property uuid [:index/name] (str capability "-capability"))
        (builder/build-property uuid [:index/capability-name] capability)
        (builder/build-property uuid [:index/headline] (str  "capability " capability)))))

(defn build-user
  [actions user password]
  (let [uuid (keyword "local" (str user "-user-uuid"))]
    (-> actions
        (builder/build-gen-uuid uuid)
        (builder/build-property uuid [:index/name] (str user "-user"))
        (builder/build-property uuid [:index/user-name] user)
        (builder/build-property uuid [:index/headline] (str  "user " user))
        (builder/build-property uuid [:content/password] password))))

(defn build-demo
  [actions]
  (-> actions
      (build-capability "welcome")
      (build-capability "profile")
      (build-capability "contacts")
      (build-capability "console")
      (build-user "Fred" "fred")
      (build-user "Sam" "sam")
      (build-user "Kris" "kris")
      ))
