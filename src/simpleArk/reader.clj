(ns simpleArk.reader
  (:require [clojure.edn :as edn]))

(defn opts-atom
  [m]
  (::opts-atom m))

(defn opts
  [m]
  @(opts-atom m))

(defn register-tag-parser!
  [m tag f]
  (swap! (opts-atom m)
         (fn [old]
           (assoc-in old [:readers tag] f))))

(defn read-string
  [m s]
  (edn/read-string (opts m) s))

(defn builder
  []
  (fn [m] (assoc m ::opts-atom (atom nil))))
