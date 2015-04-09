(require '[clojure.set :as set])
(require '[cheshire.core :as cc])
(require '[squeezer.core :as sc])
(require '[clj.common :as common])

(defn extract-referenced-ids-from-tag [t]
  (into #{}
    (mapcat :judgmentIds (:value t))))

(defn merge-referencing-ids-dicts
  ([] {})
  ([d1 d2] (merge-with set/union  d1 d2)))

(defn conv-tag-to-referencing-ids-dict [ t ]
  (let [
          id
            (:judgmentId t)
          referenced-ids
            (extract-referenced-ids-from-tag t)
          referencing-ids-dicts
            (map
              #(array-map %1 (hash-set %2))
              referenced-ids
              (repeat id))
        ]
    (reduce merge-referencing-ids-dicts referencing-ids-dicts)))

(defn conv-fname-to-referencing-id-dicts [ inp-fname ]
  (reduce merge-referencing-ids-dicts
    (map conv-tag-to-referencing-ids-dict (common/read-json inp-fname))))

(defn run [argv]
  (let [
         out-fname
           (first argv)
         inp-fnames
           (rest argv)
         res
           (reduce merge-referencing-ids-dicts
             (map conv-fname-to-referencing-id-dicts inp-fnames))
        ]
    (sc/spit-compr
      out-fname
      (cc/generate-string res {:pretty true}))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
