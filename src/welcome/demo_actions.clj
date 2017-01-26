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

(defmethod actions/action :user
  [v ark-db [kw user password]]
  (let [user-kw (keyword "local" (str user "-user"))]
    (-> v
        (actions/action ark-db [:gen-uuid user-kw])
        (actions/action ark-db [:index/name user-kw [:index/name] (str user "-user")])
        (actions/action ark-db [:index/user-name user-kw [:index/user-name] user])
        (actions/action ark-db [:index/headline user-kw [:index/headline] (str  "user " user)])
        (actions/action ark-db [:index/headline user-kw [:content/password] password]))))
