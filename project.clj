(defproject
  boot-project
  "0.0.0-SNAPSHOT"
  :dependencies
  [[org.clojure/clojure "1.9.0-alpha6" :scope "provided"]
   [danlentz/clj-uuid "0.1.6"]
   [adzerk/boot-test "1.1.1" :scope "test"]]
  :source-paths
  ["test/clj" "src/clj"])
