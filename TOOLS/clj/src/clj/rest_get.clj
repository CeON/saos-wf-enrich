(ns clj.rest-get
  (require [cheshire.core :as cc]
           [clj-http.client :as hc]
           [pjstadig.assertions :as pa]
           [squeezer.core :as sq]))

(defn ^:private die!! [ msg ]
  (println msg)
  (System/exit 1))

(defn ^:private save-data [ fname data ]
  (sq/spit-compr fname (cc/generate-string data {:pretty true})))

(defn ^:private get-url-next [ links ]
  (let [
          is-rel-next-fn?
           #(if (= (:rel % ) "next")
              (:href %)
              nil)
        ]
  (some is-rel-next-fn? links)))

(defn ^:private fetch-items [ url ]
  (let [
         _ (println url)
         response
           (try
             (hc/get url {:insecure? true})
           (catch Exception e
             { :error
                (str "Exception: " (.getMessage e))}))
         error
           (if (contains? response :error)
             (:error response)
             (if (not= (:status response) 200)
               (str
                 (:stauts response) "\n"
                 (:body response))
               nil))
         get-items-and-url-next-fn
           #(vector
              (get % :items []) (get-url-next (:links %)))
         [items url-next]
           (if error
             [ [] nil ]
             (get-items-and-url-next-fn

               (cc/parse-string (:body response) true)))
       ]
    [items url-next error]))

(defn ^:private fetch-and-aggr-items [[items url-next error]]
  (let [
         [items-new url-next-new error-new ]
          (fetch-items url-next)
      ]
    [ (concat items items-new) url-next-new error-new ]))

(defn ^:private is-there-more? [[items url-next error]]
  (and url-next (not error)))

(defn fetch-items-all [url]
  (first
    (drop-while
      is-there-more?
      (iterate fetch-and-aggr-items [[] url nil]))))

(defn ^:private create-empty-buffer [ court-type->out-fname-format ]
  (zipmap
    (keys court-type->out-fname-format)
    (map #(array-map :out-fname-format % :num-dumps 0 :data [])
      (vals court-type->out-fname-format))))

(defn ^:private filter-court-type [ court-type items ]
  (filter #(= court-type (:courtType %)) items))

(defn ^:private update-buffer-one-court-type [ court-type buffer items ]
  (update-in buffer [ court-type :data ]
    #(concat % (filter-court-type court-type items))))

(defn ^:private update-buffer [ buffer items ]
  (let [
        update-all-court-types-fn
          (apply comp
            (map
              #(fn [b] (update-buffer-one-court-type % b items))
              (keys buffer)))
       ]
  (update-all-court-types-fn buffer)))

(defn ^:private save-buffer-dump-one-court-type [ n court-type buffer ]
  (if (<= (count (get-in buffer [ court-type :data ])) n)
    buffer
    (let [
          [ dropper-f taker-f]
            (if (= n 0)
              [ (fn [seq] []) identity ]
              [  #(drop n %) #(take n %) ])
          buffer*
             (-> buffer
                (update-in [ court-type :data ] dropper-f)
                (update-in [ court-type :num-dumps ] inc))
          fname
             (format
               (get-in buffer [ court-type :out-fname-format ])
               (get-in buffer* [ court-type :num-dumps ]))
          _
            (save-data
               fname
               (taker-f
                 (get-in buffer [ court-type :data ])))
          ]
      (recur n court-type buffer*))))

(defn ^:private save-buffer-overhead [ buffer ]
  (let [
        save-all-court-types-fn
          (apply comp
            (map
              #(fn [b]
                 (save-buffer-dump-one-court-type 200 % b))
              (keys buffer)))
       ]
    (save-all-court-types-fn buffer)))

(defn ^:private save-buffer-rest [buffer]
  (let [
        save-all-court-types-fn
          (apply comp
            (map
              #(fn [b]
                 (save-buffer-dump-one-court-type 0 % b))
              (keys buffer)))
       ]
    (save-all-court-types-fn buffer)))

(defn ^:private handle-buffer [ transform-item-f [buffer url error] ]
  (let [
          [items url-next error-next]
            (fetch-items url)
          items*
            (map transform-item-f items)
           buffer*
             (if (and url-next (not error-next))
               (save-buffer-overhead
                 (update-buffer buffer items*))
               (save-buffer-rest
                 (save-buffer-overhead
                   (update-buffer buffer items*))))
        ]
   [ buffer* url-next error-next ]))

(defn fetch-buffer-all
  ([ url court-type->out-fname-format ]
  (fetch-buffer-all url court-type->out-fname-format identity))
  ([ url  court-type->out-fname-format transform-f ]
    (let [
           empty-buffer
            (create-empty-buffer court-type->out-fname-format)
           handle-buffer-f
            (partial handle-buffer transform-f)
           [buffer url-next error]
	     (first
               (drop-while
                 is-there-more?
                 (iterate handle-buffer-f [ empty-buffer url nil ])))
         ]
	error)))

; Logic for fetching divisions and chambers names

;; Common Courts

(defn ^:private gen-division-id->cc-division-items [ court ]
  (let [
         empty-court
           (dissoc court :divisions)
         divisions
           (:divisions court)
         create-division-item-fn
           #(vector (:id %) (assoc % :court empty-court))
       ]
    (map create-division-item-fn divisions)))

(defn ^:private gen-division-id->cc-division-map [ courts ]
  (into
    {}
    (mapcat gen-division-id->cc-division-items courts)))

(defn fetch-common-court-divisions [ saos-api-dump-url ]
  (let [
         [ common-courts url error]
           (fetch-items-all
             (str saos-api-dump-url "commonCourts?pageSize=100&pageNumber=0"))
        ]
    (if-not error
      [ (gen-division-id->cc-division-map common-courts) nil]
      [ {} error ])))

;; Supreme Courts

(defn ^:private gen-division-id->sc-division-items [ chamber ]
  (let [
         empty-chamber
           (dissoc chamber :divisions)
         divisions
           (:divisions chamber)
         create-division-item-fn
           #(vector (:id %) (assoc % :chamber empty-chamber))
       ]
    (map create-division-item-fn divisions)))

(defn ^:private gen-division-id->sc-division-map [ chambers ]
  (into
    {}
    (mapcat gen-division-id->sc-division-items chambers)))

(defn ^:private gen-chamber-id->sc-chamber-map [chambers]
  (let [
         chamber-keys
           (map :id chambers)
         chamber-values
           (map #(dissoc % :divisions) chambers)
        ]
        (zipmap chamber-keys chamber-values)))

(defn fetch-supreme-court-divisions [ saos-api-dump-url ]
  (let [
         [supreme-court-chambers url error ]
           (fetch-items-all
             (str saos-api-dump-url
               "scChambers?pageSize=100&pageNumber=0"))
        ]
    (if-not error
      [ (gen-division-id->sc-division-map supreme-court-chambers)
        (gen-chamber-id->sc-chamber-map supreme-court-chambers) nil]
      [  {} {} error])))
