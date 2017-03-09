(ns simpleArk.builder
  #?(:clj
     (:require
       [simpleArk.arkDb.ark-db :as ark-db]
       [console.server :as console]
       [simpleArk.mapish :as mapish])
     :cljs
     (:require
       [tiples.client :as tiples]
       [simpleArk.mapish :as mapish])))

#?(:clj
   (set! *warn-on-reflection* true))

(defmulti
  pretty-action
  (fn [v]
    (mapish/action-type v)))

(defmethod pretty-action :default
  [v]
  (pr-str v))

(defn build-property
  [actions rolon-uuid path value]
  (conj actions [(first path) rolon-uuid path value]))

(defn build-je-property
  [actions path value]
  (conj actions [(first path) :local/je path value]))

(defmethod pretty-action :property
  [[kw rolon-uuid path value]]
  (str "property (uuid " rolon-uuid ")." (pr-str path) " <= " (pr-str value)))

(defn build-relation
  [actions kw label uuid-a uuid-b value]
  (conj actions [kw label uuid-a uuid-b value]))

(defmethod pretty-action :relation
  [[kw label uuid-a uuid-b value]]
  (let [rt (cond
             (mapish/bi-rel? kw) "<->"
             (mapish/rel? kw) "->"
             (mapish/inv-rel? kw) "<-")]
    (if (some? label)
      (str "relation (" kw ")." label " (uuid " uuid-a ") " rt " (uuid " uuid-b ") " value)
      (str "relation (" kw ") (uuid " uuid-a ") " rt " (uuid " uuid-b ") " value))))

(defn build-locate-first
  [actions local-kw index-kw value]
  (conj actions [:read-index-uuid local-kw index-kw value]))

(defn build-gen-uuid
  [actions s]
  (conj actions [:gen-uuid s]))

(defn build-delete-rolon
  [actions uuid]
  (conj actions [:delete-rolon uuid]))

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
      (transaction! ark-db nil nil local actions))
     ([ark-db user-uuid capability local actions]
      (let [je-uuid
            (ark-db/process-transaction!
              ark-db
              user-uuid
              capability
              :actions-transaction!
              (pr-str [local actions]))]
        (console/notify-colsole)
        je-uuid))))
