(ns clj.common
  (require
    [cheshire.core :as cc]
    [squeezer.core :as sqc]))

(defn read-json
  ([fname ]
     (read-json fname true))
  ([fname expand-to-keywords]
    (with-open [r (sqc/reader-compr fname)]
      (into []
        (cc/parse-stream r expand-to-keywords)))))
