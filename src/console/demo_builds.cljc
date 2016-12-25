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
        (builder/build-property uuid [:index/capability-name] capability)
        (builder/build-property uuid [:index/headline] (str capability " capability")))))

(defn build-capabilities
  [actions]
  (-> actions
      (build-capability "welcome")
      (build-capability "profile")
      (build-capability "contacts")
      (build-capability "console")))
