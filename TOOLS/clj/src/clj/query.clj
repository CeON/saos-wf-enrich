(ns clj.query
  (require
           [ clojure.string :as str]
           [ squeezer.core :as sqc]
           [ cheshire.core :as cc]
           [ paralab.core :as pc]
           [ paralab.fj-core :as fjc]
           [ paralab.fj-tasks :as fjt])
  (:gen-class))

(defn print-json [data]
  (println
    (cc/generate-string data {:pretty true})))

(defn print-plain [data]
  (doall (map #(println %) data)))

(defn keys-sorted-by-val-desc [ m ]
  (sort-by m #(compare %2 %1) (keys m)))

(defn print-freq-desc [ freq ]
  (doall
    (map
       #(println (format "%10d %s"(get freq %) %))
       (keys-sorted-by-val-desc freq))))

(defn is-n-cpu-opt? [opt]
  (some? (re-matches #"-p[0-9]*" opt)))

(defn get-n-cpus [ opt ]
  (let [
         tot-n-cpus
           (pc/get-n-cpus)
         n-cpus-str
           (str/replace opt #"-p" "")
         n-cpus
           (if (empty? n-cpus-str)
             tot-n-cpus
             (Integer. n-cpus-str))
        ]
       n-cpus))

;; -p1 is the only way to enable serial computation

(defn -main [ & args ]
  (let [
         [ n-cpus query-fname data-fnames ]
           (if (is-n-cpu-opt? (first args))
             [ (get-n-cpus (first args))
               (second args)
               (into [] (drop 2 args)) ]
             [ (pc/get-n-cpus)
               (first args)
               (into [] (rest args)) ])
        { :keys [transform-f merge-f print-f]
            :or { merge-f concat
                  print-f print-json }}
            (load-file query-fname)
         transform-f*
           (fn [fname]
             (let [
                    data (cc/parse-stream
                            (sqc/reader-compr fname) true)
                  ]
               (transform-f data)))
         fj-task
           (fjt/make-fj-task-map-reduce-vec
                 :data data-fnames
                 :map-f transform-f*
                 :reduce-f merge-f
                 :size-threshold 2)
         res
           (if (> n-cpus 1)
             (fjt/run-fj-task
               (fjc/make-fj-pool n-cpus)
               fj-task)
             (fjt/run-fj-task-serial fj-task))
        ]
     (print-f res)))
