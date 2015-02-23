(ns clj.query
  (require [ squeezer.core :as sqc]
           [ cheshire.core :as cc]
           [ paralab.fj-core :as fjc]
           [ paralab.fj-tasks :as fjt])
  (:gen-class))

(defn print-json [data]
  (println
    (cc/generate-string data {:pretty true})))

(defn print-plain [data]
  (doall (map #(println %) data)))

(defn -main
  [ & args ]
  (let [
         { :keys [transform-f merge-f print-f]
            :or { merge-f concat
                  print-f print-json }}
            (load-file (first args))
         transform-f*
           (fn [fname]
             (let [
                    data (cc/parse-stream
                            (sqc/reader-compr fname) true)
                  ]
               (transform-f data)))
         fnames
           (into [] (rest args))
         fj-task
           (fjt/make-fj-task-map-reduce-vec
                 :data fnames
                 :map-f transform-f*
                 :reduce-f merge-f
                 :size-threshold 2)
         fj-pool
           (fjc/make-fj-pool)
         res
           (fjt/run-fj-task fj-pool fj-task)
        ]
     (print-f res)))