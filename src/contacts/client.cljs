(ns contacts.client
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [clojure.string :as string]
    [tiples.client :as tiples]
    [tiples.login :as login]))

(j/defc= my-contacts
       (if login/common-data
         (:contacts login/common-data)
         nil)
       (partial swap! login/common-data assoc :contacts))

(defn delete-contact [contact]
  (tiples/chsk-send! [:contacts/delete {:contact contact}]))

(defmethod tiples/chsk-recv :contacts/deleted
  [id contact]
  (.log js/console "deleted contact")
  (swap! my-contacts disj contact))

(defmethod tiples/chsk-recv :contacts/added
  [id contact]
  (.log js/console "added contact")
  (swap! my-contacts conj contact))

(defn add-contact [contact]
  (tiples/chsk-send! [:contacts/add {:contact contact}]))

(defn middle-name [{:keys [middle middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

(defn display-name [{:keys [first last] :as contact}]
  (str last ", " first (middle-name contact)))

(h/defelem contact-list [{:keys [from sorted-by] :or {sorted-by identity}}]
         (h/loop-tpl :bindings [contact (j/cell= (sort-by sorted-by from))]
                   (h/tr (h/td (j/cell= (display-name contact)))
                       (h/td (h/button :click #(delete-contact @contact) "Delete")))))

(defn parse-contact [contact-str]
  (let [[first middle last :as parts] (string/split contact-str #"\s+")
        [first last middle] (if (nil? last) [first middle] [first last middle])
        middle (when middle (string/replace middle "." ""))
        c (if middle (count middle) 0)]
    (when (>= (count parts) 2)
      (cond-> {:first first :last last}
              (== c 1) (assoc :middle-initial middle)
              (>= c 2) (assoc :middle middle)))))

(h/defelem contact-input [{:keys [to]} [label]]
         (let [new-contact (j/cell "")
               parsed (j/cell= (parse-contact new-contact))]
           (h/div
             (h/input
               :value new-contact
               :input #(reset! new-contact @%))
             (h/button
               :click #(when-let [c @parsed]
                         (j/dosync (to c)
                                 (reset! new-contact "")))
               :disabled (j/cell= (not parsed))
               label)
             (h/pre (j/cell= (pr-str parsed))))))

(def do-contacts
  (h/div
    (h/div :id "header"
         :style "background-color:#fff0f0"
         (login/tabs-div)
         (h/h2 "Contact list"))
    (h/table (contact-list :from my-contacts :sorted-by :last))
    (contact-input :to add-contact "Add contact")
    (h/hr)
    (h/p (h/em "Note: The Add contact button is disabled until you enter a valid contact.  A valid contact consists of two or three whitespace-delimited names."))))

(defmethod login/add-element :contacts [_]
  (do-contacts))
