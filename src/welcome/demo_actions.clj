(ns welcome.demo-actions
  (:require [simpleArk.actions :as actions]))

(defmethod actions/action :capability
  [v ark-db [kw capability]]
  (let [capability-kw (keyword "local" (str capability "-capability"))]
    (-> v
        (actions/action ark-db [:gen-uuid capability-kw])
        (actions/action ark-db [:index/name capability-kw [:index/name] (str capability "-capability")])
        (actions/action ark-db [:index/capability-name capability-kw [:index/capability-name] capability])
        (actions/action ark-db [:index/headline capability-kw [:index/headline] (str  "capability " capability)]))))
