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
  [actions kw vec-a vec-b]
  (conj actions [kw vec-a vec-b]))

(defn build-locate-first
  [actions local-kw index-kw value]
  (conj actions [:read-index-uuid local-kw index-kw value]))

(defn build-gen-uuid
  [actions s]
  (conj actions [:gen-uuid s]))

(defn build-println
  [actions s]
  (conj actions [:println s]))

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
