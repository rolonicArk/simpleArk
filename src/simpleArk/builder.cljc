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
  [actions kw uuid-a label-a uuid-b label-b]
   (conj actions [kw uuid-a label-a uuid-b label-b]))

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

(defn build-replace-map
  [actions m prefix rolon-uuid]
  (conj actions [:replace-map m prefix rolon-uuid]))

#?(:clj
   (defn transaction!
     ([ark-db local actions]
      (transaction! ark-db nil local actions))
     ([ark-db user-uuid local actions]
     (ark-db/process-transaction!
       ark-db
       user-uuid
       :actions-transaction!
       (pr-str [local actions]))))
   :cljs
   (defn transaction!
     [local actions]
     (tiples/chsk-send!
       [:console/process-transaction
        {:tran-keyword :actions-transaction!
         :tran-data    (pr-str [local actions])}])))
