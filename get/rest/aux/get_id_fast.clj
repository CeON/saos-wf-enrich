(require
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[paralab.fj-core :as fjc]
   '[paralab.fj-tasks :as fjt])

(defn filter-matching-recs-from-file [ fname filter-f ]
  (as-> fname ---
        (sc/slurp-compr ---)
        (cc/parse-string --- true)
        (filter filter-f ---)
        (into [] ---)))

(defn run [argv]
  (let [
          id-set
             (->> (first argv)
               slurp
               str/split-lines
               (map #(Integer. %))
               (into #{}))
           map-f
             (fn [fname]
               (filter-matching-recs-from-file
                 fname
                 #(contains? id-set (:id %))))
           reduce-f
              concat
          fnames
           (into [] (rest argv))
         fj-task
           (fjt/make-fj-task-map-reduce-vec
                 :data fnames
                 :map-f map-f
                 :reduce-f concat
                 :size-threshold 2)
         fj-pool
           (fjc/make-fj-pool)
         res
           (fjt/run-fj-task fj-pool fj-task)
        ]
      (println
        (cc/generate-string res {:pretty true}))))


(when (> (count *command-line-args*) 0)
  (run *command-line-args*))
