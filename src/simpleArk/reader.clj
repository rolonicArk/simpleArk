(ns simpleArk.reader
  (:require [clojure.edn :as edn])
  (:refer-clojure :exclude [read read-string]))

(set! *warn-on-reflection* true)

(defn opts-atom
  [component-map]
  (::opts-atom component-map))

(defn opts
  [component-map]
  @(opts-atom component-map))

(defn register-tag-parser!
  [component-map tag f]
  (swap! (opts-atom component-map)
         (fn [old]
           (assoc-in old [:readers tag] f))))

(defn read-string
  [component-map s]
  (edn/read-string (opts component-map) s))

(defn builder
  []
  (fn [component-map] (assoc component-map ::opts-atom (atom nil))))
