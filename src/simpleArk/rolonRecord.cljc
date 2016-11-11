(ns simpleArk.rolonRecord)

#?(:clj
   (set! *warn-on-reflection* true))

(defrecord Rolon-record [rolon-uuid])

(defn create-rolon [m]
  (let [rolon (->Rolon-record (:rolon-uuid m))]
    (assoc rolon :changes-by-property (:changes-by-property m))))

#?(:cljs
   (cljs.reader/register-tag-parser! "simpleArk.rolonRecord.Rolon-record" create-rolon))
