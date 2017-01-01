(ns console.path-commands
  (:require
    [hoplon.core :as h]
    [javelin.core :as j]
    [console.client :as client]
    [simpleArk.uuid :as suuid]
    [simpleArk.arkRecord :as arkRecord]
    [simpleArk.mapish :as mapish]))

(defn list-changes
  [ark-record]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "list property changes over time for " client/command-prefix-style)
  (let [path @client/selected-path
        uuid (suuid/create-uuid @client/selected-rolon)
        changes (arkRecord/get-changes-by-property ark-record uuid path)]
    (client/history-path! ark-record path)
    (client/add-history! " in ")
    (client/add-history!
      (client/pretty-uuid ark-record uuid)
      (client/clickable-styles uuid)
      client/uuid-click
      @client/selected-rolon)
    (client/add-history! "\n")
    (client/clear-output!)
    (client/add-output! "changes over time for ")
    (client/output-path! ark-record path)
    (client/add-output! " in ")
    (client/add-output!
      (client/pretty-uuid ark-record uuid)
      (client/clickable-styles uuid)
      client/uuid-click
      @client/selected-rolon)
    (client/add-output! "\n\n")
    (reduce
      (fn [_ [[ts] v]]
        (let [je-uuid (arkRecord/get-journal-entry-uuid ark-record ts)]
          (client/add-output! (client/pretty-uuid
                                ark-record
                                je-uuid)
                              (client/clickable-styles uuid)
                              client/uuid-click
                              (str je-uuid))
          (client/add-output! " ")
          (client/add-output! (pr-str v))
          (client/add-output! "\n")))
      nil changes)))

(defn explore
  [ark-record uuid path]
  (client/add-prompt)
  (client/add-history! ">")
  (client/add-history! "explore " client/command-prefix-style)
  (client/history-path! ark-record path)
  (client/add-history! " in ")
  (client/add-history!
    (client/pretty-uuid ark-record uuid)
    (client/clickable-styles uuid)
    client/uuid-click
    @client/selected-rolon)
  (client/add-history! "\n")
  (client/clear-output!)
  (client/add-output! "explore ")
  (client/output-path! ark-record path)
  (client/add-output! " in ")
  (client/add-output!
    (client/pretty-uuid ark-record uuid)
    (client/clickable-styles uuid)
    client/uuid-click
    @client/selected-rolon)
  (client/add-output! "\n")
  (let [ptree (arkRecord/get-property-tree ark-record uuid path)
        pm (first ptree)
        pval (if (= 0 (count path))
               nil
               (arkRecord/get-property-value ark-record uuid path))]
    (when (some? pval)
      (client/add-output! "\n   ")
      (client/output-path! ark-record path)
      (client/add-output! (str " = " (pr-str pval))))
    (when (some? pm)
      (reduce
        (fn [_ e]
          (let [k (key e)
                e-path (into path k)
                pt (val e)
                count (if (vector? pt)
                        (arkRecord/tree-count ark-record pt)
                        (arkRecord/tree-count ark-record e))]
            (if (< 0 count)
              (do
                (client/add-output! "\n")
                (client/output-path! ark-record e-path)
                (client/add-output! (str " : " count))))
            )
          nil)
        nil
        pm))
    (client/add-output! (str "\n\ntotal: " (arkRecord/tree-count ark-record ptree))))
  )

(defn do-path-commands
  []
  (h/div

    (h/div
      (h/strong "Selected path: ")
      (client/display-selected-path))

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (not= "" client/selected-rolon))
      :click (fn []
               (reset! client/display-mode 0)
               (explore @client/my-ark-record (suuid/create-uuid @client/selected-rolon) @client/selected-path))
      "explore properties")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (< 0 (count client/selected-path)))
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "clear path selection\n" client/command-prefix-style)
               (reset! client/selected-path []))
      "clear path selection")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (< 0 (count client/selected-path)))
      :click (fn []
               (reset! client/display-mode 0)
               (client/add-prompt)
               (client/add-history! ">")
               (client/add-history! "up\n" client/command-prefix-style)
               (swap! client/selected-path pop))
      "up")

    (h/button
      :css {:display "none" :background-color "MistyRose"}
      :toggle (j/cell= (and
                         (< 0 (count client/selected-path))
                         (not= "" client/selected-rolon)
                         (some?
                           (arkRecord/get-changes-by-property
                             client/my-ark-record
                             (suuid/create-uuid client/selected-rolon)
                             client/selected-path))))
      :click (fn []
               (reset! client/display-mode 0)
               (list-changes @client/my-ark-record))
      "list property changes over time")
    ))
