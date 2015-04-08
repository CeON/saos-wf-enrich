(require
  '[cheshire.core :as cc]
  '[squeezer.core :as sc]
  '[hiccup.page :as hp]
  '[clj.query :as q])

(defn conv-tag-to-list-item [ tag ]
  (let [
         id
           (:id tag)
         keywords
           (q/keys-sorted-by-val-desc
             (get-in tag [:value :keywordsISAP]))
         keywords-items
           (if (empty? keywords)
             "brak"
             [ :ul
               (map #(vector :li %) keywords) ])
       ]
    [ :li
        [ :a {:href (str "https://saos-test.icm.edu.pl/judgments/" id)}
             "SAOS id: " id ] [ :br ]
        [ :p "Słowa kluczowe: " keywords-items]]))

(defn process [inp-fname out-fname]
  (let [
         inp-data
           (-> inp-fname
             sc/slurp-compr
             (cc/parse-string true))
          out-list-items
            (map
               conv-tag-to-list-item
               inp-data)
          html5-str
           (hp/html5
               [:head
                 [:meta
                 {:http-equiv "content-type", :content "text/html;charset=utf-8"} ]]
               [:body [ :ol out-list-items ]])
       ]
    (sc/spit-compr
      out-fname
      html5-str)))

(defn run [argv]
  (let [
         n
           (quot (count argv) 2)
         inp-fnames
           (take n argv)
         out-fnames
           (drop n argv)
        ]
  (dorun
    (map
      process
      inp-fnames out-fnames))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
