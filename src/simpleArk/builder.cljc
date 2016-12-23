(ns simpleArk.builder)

#?(:clj
   (set! *warn-on-reflection* true))

(defn build-property
  [actions rolon-uuid path value]
  (conj actions [(first path) rolon-uuid path value]))

(defn build-je-property
  [actions path value]
  (conj actions [(first path) :je path value]))

(defn build-println
  [actions s]
  (conj actions [:println s]))
