(ns console.server
  (:require [tiples.users :as users]))

(users/add-capability :console)
