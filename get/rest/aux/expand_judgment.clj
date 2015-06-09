(require
   '[riffle.write :as rw]
   '[riffle.read :as rr]
   '[taoensso.nippy :as nippy]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[me.raynes.fs :as fs]
   '[clj.common :as cljc])

(def RIFFLE-FNAME "judgments.riffle")

(defn make-kv-pairs [ fname ]
  (let [
         data (cljc/read-json fname)
         ks
           (map #(str (:id %)) data)
         vs
           (map nippy/freeze data)
         ]
  (map vector ks vs)))

(defn create-riffle-store [ riffle-fname inp-json-files]
  (if-not  (empty?  inp-json-files)
    (if-not (fs/exists? riffle-fname)
      (do
        (println (str "Creating riffle store " riffle-fname "..."))
        (rw/write-riffle
          (mapcat make-kv-pairs inp-json-files)
          riffle-fname))
      (println (str "Riffle store " riffle-fname " found, skipping creation.")))
    (if-not (fs/exists? riffle-fname)
      (do
        (println
          (str "No data files provided and no riffle store " riffle-fname
               " found. Exiting ..."))
        (System/exit 1))
      (println
        (str "No data files provided, skipping riffle store "
             riffle-fname " creation.")))))

(defn expand-tag [ riffle-store tag ]
  (let [
          id (str (:judgmentId tag))
          val (:value tag)
          j (nippy/thaw
              (rr/get riffle-store id))
        ]
   (assoc j :tag val)))

(defn run [ argv ]
  (let [

         tag-data (cljc/read-json (first argv))
         expanded-tag-fname (second argv)
         inp-json-files (drop 2 argv)
         _ (create-riffle-store RIFFLE-FNAME inp-json-files)
         _ (println "Expanding tags...")
         riffle-store (rr/riffle RIFFLE-FNAME)
         expanded-tag-data
           (into []
             (map (partial expand-tag riffle-store) tag-data))
        ]
    (cljc/write-json expanded-tag-fname expanded-tag-data)))


(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
