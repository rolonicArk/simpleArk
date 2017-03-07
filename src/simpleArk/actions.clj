(ns simpleArk.actions
  (:require [simpleArk.mapish :as mapish]
            [simpleArk.arkValue.ark-value :as ark-value]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.arkDb.ark-db :as ark-db]
            [simpleArk.uuid.uuid :as suuid]))

(set! *warn-on-reflection* true)

(defmulti
  action
  (fn [state ark-db v]
    (mapish/action-type v)))

(defn eval-actions
  [state ark-db actions]
  (reduce
    (fn [state v]
      (action state ark-db v))
    state
    actions))

(defmethod ark-value/eval-transaction :actions-transaction!
  [ark-record ark-db n s]
  (let [[local actions] (read-string s)]
    (second (eval-actions [local ark-record] ark-db actions))))

#_(defn process-actions!
  [ark-db user-uuid capability local actions]
  (let [s (pr-str [local actions])]
    (ark-db/process-transaction! ark-db user-uuid capability :actions-transaction! s)))

(defn fetch
  [ark-record local s]
  (if (and (keyword? s) (= "local" (namespace s)))
    (if (= "je" (name s))
      (arkRecord/get-latest-journal-entry-uuid ark-record)
      (if (not (contains? local s))
        (throw (Exception. (str "Not defined: " s)))
        (s local)))
    s))

(defn make-rolon
  [ark-record rolon-uuid]
  (if (arkRecord/get-rolon ark-record rolon-uuid)
    ark-record
    (ark-value/assoc-rolon
      ark-record
      rolon-uuid
      (rolonRecord/->Rolon-record rolon-uuid))))

(defmethod action :property
  [[local ark-record] ark-db [kw rolon-uuid path value]]
  (let [rolon-uuid (fetch ark-record local rolon-uuid)
        _ (if (arkRecord/deleted? ark-record rolon-uuid)
            (throw (Exception. "Already deleted")))
        path (fetch ark-record local path)
        value (fetch ark-record local value)
        ark-record (make-rolon ark-record rolon-uuid)
        ark-record (ark-value/update-property ark-record ark-db rolon-uuid path value)]
    [local ark-record]))

(defmethod action :relation
  [[local ark-record] ark-db v]
  (let [[kw label uuid-a uuid-b value] v
        relaton-name (name kw)
        label (fetch ark-record local label)
        uuid-a (fetch ark-record local uuid-a)
        uuid-b (fetch ark-record local uuid-b)
        namespace (namespace kw)
        inv (= namespace "inv-rel")
        from-uuid (if inv uuid-b uuid-a)
        to-uuid (if inv uuid-a uuid-b)
        symetrical (= "bi-rel" namespace)
        ark-record (if (or
                         (arkRecord/deleted? ark-record uuid-a)
                         (arkRecord/deleted? ark-record uuid-b))
                     (throw (Exception. "Already deleted"))
                     (ark-value/update-relation
                       ark-record
                       ark-db
                       relaton-name label
                       from-uuid to-uuid
                       symetrical value))]
    [local ark-record]))

(defmethod action :locate-first-uuid
  [[local ark-record] ark-db [kw local-kw index-kw value]]
  (let [index-kw (fetch ark-record local index-kw)
        value (fetch ark-record local value)
        index-uuid (arkRecord/get-index-uuid ark-record (name index-kw))
        rolon-uuid (if (nil? index-uuid)
                     nil
                     (first (arkRecord/index-lookup ark-record index-uuid value)))
        local (assoc local local-kw rolon-uuid)]
    [local ark-record]))

(defmethod action :gen-uuid
  [[local ark-record] ark-db [kw s]]
  (let [local (assoc local s (suuid/random-uuid ark-db))]
    [local ark-record]))

(defmethod action :delete-rolon
  [[local ark-record] ark-db [kw rolon-uuid]]
  (let [rolon-uuid (fetch ark-record local rolon-uuid)]
    (cond
      (suuid/journal-entry-uuid? rolon-uuid)
      (throw (Exception. "Journal entry rolons can not be deleted."))
      (suuid/index-uuid? rolon-uuid)
      (throw (Exception. "Index rolons can not be deleted."))
      (suuid/random-uuid? rolon-uuid)
      ()
      :else
      (throw (Exception. (str "Unrecognized UUID: " rolon-uuid))))
    (if (arkRecord/deleted? ark-record rolon-uuid)
      (throw (Exception. "Already deleted"))
      (let [je-uuid (arkRecord/get-latest-journal-entry-uuid ark-record)
            old-property-values (arkRecord/get-property-values ark-record rolon-uuid)
            ark-record (reduce
                         (fn [ark-record e]
                           (let [path (key e)
                                 kw (first path)
                                 relation-name (name kw)
                                 label (if (= (count path) 3) (second path) nil)
                                 uuid (last path)]
                             (cond
                               (or (mapish/index? kw) (mapish/content? kw))
                               (ark-value/update-property- ark-record ark-db je-uuid rolon-uuid path nil)
                               (mapish/bi-rel? kw)
                               (ark-value/update-relation ark-record ark-db relation-name label rolon-uuid uuid true nil)
                               (or (mapish/rel? kw) (mapish/inv-rel? kw))
                               (if (= kw :inv-rel/modified)
                                 ark-record
                                 (ark-value/update-relation ark-record ark-db relation-name label rolon-uuid uuid false nil))
                               :else
                               ark-record)))
                         ark-record
                         (seq old-property-values))
            ark-record (ark-value/update-property ark-record ark-db rolon-uuid [:content/deleted] true)]
        [local ark-record]))))

(defmethod action :println
  [[local ark-record] ark-db [kw s]]
  (cond
    (vector? s)
    (println
      (apply
        str
        (map
          (fn [a]
            (fetch ark-record local a))
          s)))
    (= :local s)
    (println (pr-str local))
    :else
    (println (fetch ark-record local s)))
  [local ark-record])

(defmethod action :exception
  [[local ark-record] ark-db [kw s]]
  (println "throwing exception")
  (throw (Exception. (str (fetch ark-record local s)))))

(defmethod action :replace-map
  [[local ark-record] ark-db [kw m prefix rolon-uuid]]
  (let [m (fetch ark-record local m)
        prefix (fetch ark-record local prefix)
        rolon-uuid (fetch ark-record local rolon-uuid)
        mi (reduce
             (fn [m e]
               (assoc m (conj prefix (key e)) (val e)))
             (ark-value/create-mi ark-db)
             m)
        mi (reduce
             (fn [m e]
               (let [k (key e)]
                 (if (contains? m k)
                   m
                   (assoc m k nil))))
             mi
             (mapish/mi-sub
               (arkRecord/get-property-values ark-record rolon-uuid)
               prefix))
        ark-record (ark-value/make-rolon ark-record ark-db rolon-uuid mi)]
    [local ark-record]))
