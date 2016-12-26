(ns console.demo
  (:require [console.server :as console]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.uuid :as suuid]
            [simpleArk.builder :as builder]
            [simpleArk.actions]
            [console.demo-builds :as demo-builds]))

(def ark-db console/ark-db)

(defn initializer
  []
  (console/initializer)
  (ark-db/open-ark! ark-db)

  (builder/transaction!
    ark-db {}
    (-> []
        (builder/build-je-property [:index/headline] "Build demo data")
        (demo-builds/build-demo)
        ;(builder/build-println :local)
        ))

  (console/update-ark-record! (ark-db/get-ark-record ark-db))
  )
