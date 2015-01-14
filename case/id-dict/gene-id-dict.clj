(require
   '[riffle.write :as rw]
   '[riffle.read :as rr]
   '[byte-streams :as bs]
   '[byte-transforms :as bt]
   '[taoensso.nippy :as nippy]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc])

(defn make-kv-pairs [ fname ]
  (let [
         data
           (cc/parse-string
             (sc/slurp-compr fname) true)
         ks
           (map #(str (:id %)) data)
         vs
           (map nippy/freeze data)
         ]
  (map vector ks vs)))

(defn run [argv]
  (let [
         kv-store-fname (first argv)
         inp-json-files (rest argv)
        ]
    (rw/write-riffle
      (mapcat make-kv-pairs inp-json-files)
      kv-store-fname)))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
