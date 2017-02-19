(set-env!
  :dependencies '[
                  [org.clojure/clojure                       "1.9.0-alpha14"  :scope "provided"]
                  [org.clojure/core.async                    "0.2.385"]
                  [org.clojure/clojurescript "1.9.473"]
                  [org.clojure/data.priority-map "0.0.7"]
                  ;[org.clojure/google-closure-library "0.0-20160609-f42b4a24"]
                  ;[org.clojure/google-closure-library-third-party "0.0-20160609-f42b4a24"]
                  [org.clojure/tools.reader  "1.0.0-beta4"]
                  [org.clojure/tools.nrepl   "0.2.12" :scope "test"]
                  [danlentz/clj-uuid                         "0.1.6"]
                  [adzerk/boot-test                          "1.1.2"         :scope "test"]
                  [adzerk/boot-cljs          "1.7.228-1"]
                  [adzerk/boot-cljs-repl     "0.3.3" :scope "test"]
                  [adzerk/boot-reload        "0.4.12" :scope "test"]
                  [com.google.guava/guava "19.0"]
                  ;[com.google.javascript/closure-compiler "v20160315"]
                  ;[com.google.javascript/closure-compiler-externs "v20160315"]
                  [ring/ring-core            "1.6.0-beta5"]
                  [ring/ring-defaults        "0.2.1"]
                  [com.cemerick/piggieback   "0.2.1" :scope "test"]
                  [weasel                    "0.7.0" :scope "test"]
                  [compojure                 "1.6.0-beta1"]
                  [hoplon/boot-hoplon        "0.3.0"]
                  [hoplon                    "6.0.0-alpha16"]
                  [pandeiro/boot-http        "0.7.3"]
                  [http-kit                  "2.2.0"]
                  [com.taoensso/sente        "1.10.0"]
                  [com.rpl/specter           "0.13.0"]
                  [com.andrewmcveigh/cljs-time "0.4.0"]
                  ]
  :source-paths #{"src" "test"}
  :resource-paths #{"assets"})

(require
  '[adzerk.boot-test            :refer :all]
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[hoplon.boot-hoplon    :refer [hoplon prerender]]
  '[pandeiro.boot-http    :refer [serve]]
  )

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
                                simpleArk.ark-db0-test
                                simpleArk.ark-dba0-test
                                simpleArk.tlog0-test
                                simpleArk.ark-dba1-test
                                simpleArk.pub0-test
                                simpleArk.tlog1-test
                                simpleArk.mapish-test
                                simpleArk.action-test
                                })))

(deftask dev
  "Build ws-simple for local development."
  []
  (comp
    (serve
      :handler 'welcome.demo/handler
      :reload true
      :port 8000
      :httpkit true
      :init 'strap/jetty-init
      )
    (watch)
    (speak)
    (hoplon)
    (reload)
    (cljs-repl)
    (cljs)))
