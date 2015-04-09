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

(defn println-err [ & args ]
  (dorun
    (map #(.println *err* %) args)))
