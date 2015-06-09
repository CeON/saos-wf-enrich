(ns clj.common
  (require
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

(defn println-err [ & args ]
  (dorun
    (map #(.println *err* %) args)))
