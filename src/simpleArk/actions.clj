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
  [[local ark-record] ark-db [kw uuid-a uuid-b]]
  (let [uuid-a (fetch local uuid-a)
        uuid-b (fetch local uuid-b)
        kw-name (name kw)
        inv-kw (cond
                 (mapish/rel? kw) (keyword "inv-rel" kw-name)
                 (mapish/inv-rel? kw) (keyword "rel" kw-name)
                 :else kw)
        ark-record (make-rolon ark-record uuid-a)
        ark-record (make-rolon ark-record uuid-b)
        ark-record (ark-value/update-property ark-record ark-db uuid-a [kw uuid-b] true)
        ark-record (ark-value/update-property ark-record ark-db uuid-b [inv-kw uuid-a] true)
        ]
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
