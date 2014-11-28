(defproject clj "0.1.0-SNAPSHOT"

  ; GENERAL OPTIONS

  :description "description"
  :url "url"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"
  :jar-name "clj.jar"
  :uberjar-name "clj-standalone.jar"
  :aot :all
  :omit-source true
  :main clj.core

  ;; Options used by Java
  ;;; run with assertions enabled
  :jvm-opts ["-ea"]

  ; DEPENDENCIES

  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [me.raynes/fs "1.4.6"]  ;; Filesystem utilities
    [cheshire "5.3.1"]  ;; JSON support
    [clj-http "1.0.1"] ;; http client 
  ]

  ; PLUGINS + CONFIGURATION

  :plugins [[codox "0.8.10"]]

  ;; codox configuration

  :codox {
          :output-dir "target/apidoc"
          :sources [ "src/"]
          :defaults {:doc/format :markdown}
          ;; Uncomment this to get github links in sources
          ;; :src-dir-uri "githubrepo/blob/master/"
          ;; :src-linenum-anchor-prefix "L"
          }
)
