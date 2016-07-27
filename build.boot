(set-env!
  :dependencies '[[org.clojure/clojure                       "1.9.0-alpha10"  :scope "provided"]
                  [danlentz/clj-uuid                         "0.1.6"]
                  [adzerk/boot-test                          "1.1.2"         :scope "test"]
                  [org.clojure/core.async                    "0.2.385"]]
  :source-paths #{"src/clj" "test/clj"})

(require
  '[adzerk.boot-test            :refer :all])

(deftask test-it
   "Setup, compile and run the tests."
   []
   (comp
     (run-tests :namespaces '#{
                                simpleArk.log0-test
                                simpleArk.logt-test
                                simpleArk.closer-test
                                simpleArk.uuid-test
                                simpleArk.uuid0-test
                                simpleArk.uuidi-test
                                simpleArk.core-test
                                simpleArk.ark-dba0-test
                                simpleArk.tlog0-test
                                simpleArk.ark-dba1-test
                                simpleArk.pub0-test
                                })))
