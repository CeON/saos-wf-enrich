(ns clj.common
  (require
    [clojure.string :as str]
    [clojure.java.io :as io]
    [clojurewerkz.propertied.properties :as p]
    [cheshire.core :as cc]
    [squeezer.core :as sc]))

(defn read-json
  ([fname ]
     (read-json fname true))
  ([fname expand-to-keywords]
     (-> fname
       sc/slurp-compr
      (cc/parse-string expand-to-keywords))))

(defn write-json [ fname data ]
  (sc/spit-compr
    fname
    (cc/generate-string data {:pretty true})))

(defn read-properties [ fname ]
  (p/properties->map
    (p/load-from (io/file fname))
    true))

(defn println-err [ & args ]
  (dorun
    (map #(.println *err* %) args)))

(defn normalize-case-number [ case-number ]
  (-> case-number
      (str/replace #"\s+" " ")
      (str/replace #"\." "")
      str/trim))
