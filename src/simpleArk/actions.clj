(ns simpleArk.actions
  (:require [simpleArk.mapish :as mapish]
            [simpleArk.ark-value :as ark-value]
            [simpleArk.arkRecord :as arkRecord]
            [simpleArk.rolonRecord :as rolonRecord]
            [simpleArk.ark-db :as ark-db]
            [simpleArk.uuid :as suuid]))

(set! *warn-on-reflection* true)

(defmulti
  action
  (fn [state ark-db v]
    (let [kw (first v)]
      (cond
        (mapish/index? kw) :property
        (mapish/content? kw) :property
        (mapish/rel? kw) :relation
        (mapish/inv-rel? kw) :relation
        (mapish/bi-rel? kw) :relation
        :else kw))))

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

(defn process-actions!
  [ark-db local actions]
  (let [s (pr-str [local actions])]
    (ark-db/process-transaction! ark-db :actions-transaction! s)))

(defn fetch
  [local s]
  (if (and (keyword? s) (= "local" (namespace s)))
    (s local)
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
  (let [rolon-uuid
        (if (= :je rolon-uuid)
          (arkRecord/get-latest-journal-entry-uuid ark-record)
          (fetch local rolon-uuid))
        path (fetch local path)
        value (fetch local value)
        ark-record (make-rolon ark-record rolon-uuid)
        ark-record (ark-value/update-property ark-record ark-db rolon-uuid path value)]
    [local ark-record]))

(defmethod action :relation
  [[local ark-record] ark-db v]
  (let [[kw label uuid-a uuid-b] v
        relaton-name (name kw)
        label (fetch local label)
        uuid-a (fetch local uuid-a)
        uuid-b (fetch local uuid-b)
        namespace (namespace kw)
        inv (= namespace "inv-rel")
        from-uuid (if inv uuid-b uuid-a)
        to-uuid (if inv uuid-a uuid-b)
        symetrical (= "bi-rel" namespace)
        ark-record (ark-value/update-relation
                     ark-record
                     ark-db
                     relaton-name label
                     from-uuid to-uuid
                     symetrical true)]
    [local ark-record]))

(defmethod action :locate-first-uuid
  [[local ark-record] ark-db [kw local-kw index-kw value]]
  (let [index-kw (fetch local index-kw)
        value (fetch local value)
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

(defmethod action :println
  [[local ark-record] ark-db [kw s]]
  (if (= :local s)
    (println (pr-str local))
    (println (fetch local s)))
  [local ark-record])

(defmethod action :exception
  [[local ark-record] ark-db [kw s]]
  (println "throwing exception")
  (throw (Exception. (str (fetch local s)))))

(defmethod action :replace-map
  [[local ark-record] ark-db [kw m prefix rolon-uuid]]
  (let [m (fetch local m)
        prefix (fetch local prefix)
        rolon-uuid (fetch local rolon-uuid)
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
