(ns simpleArk.builder
  (:require #?(:clj
                [simpleArk.ark-db :as ark-db]
               :cljs
               [tiples.client :as tiples])))

#?(:clj
   (set! *warn-on-reflection* true))

(defn build-property
  [actions rolon-uuid path value]
  (conj actions [(first path) rolon-uuid path value]))

(defn build-je-property
  [actions path value]
  (conj actions [(first path) :je path value]))

(defn build-relation
  [actions kw uuid-a uuid-b]
  (conj actions [kw uuid-a uuid-b]))

(defn build-println
  [actions s]
  (conj actions [:println s]))

(defn build-gen-uuid
  [actions s]
  (conj actions [:gen-uuid s]))

(defn build-invalid
  [actions]
  (conj actions [:invalid]))

(defn build-exception
  [actions msg]
  (conj actions [:exception msg]))

#?(:clj
   (defn transaction!
     [ark-db local actions]
     (ark-db/process-transaction!
       ark-db
       :actions-transaction!
       (pr-str [local actions])))
   :cljs
   (defn transaction!
     [local actions]
     (tiples/chsk-send!
       [:console/process-transaction
        {:tran-keyword :actions-transaction!
         :tran-data (pr-str [local actions])}])))
