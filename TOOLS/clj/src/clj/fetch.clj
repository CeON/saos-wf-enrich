(ns clj.fetch
  (require [cheshire.core :as cc]
           [clj-http.client :as hc]))

(defn ^:private get-url-next [ links ]
  (let [
          is-rel-next-fn?
           #(if (= (:rel % ) "next")
              (:href %)
              nil)
        ]
  (some is-rel-next-fn? links)))

(defn fetch-items [ url ]
  (let [
         _ (println url)
         response
           (try
             (hc/get url)
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
        _ (println url-next)
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

(def empty-buffer
  { "COMMON_COURT" { :data []
                     :num-dumps 0
                     :out-fname-format
                        "out/commo_court_%04d.json"
                   }
    "SUPREME_COURT" { :data []
                      :num-dumps 0
                      :out-fname-format
                        "out/supre_court_%04d.json"
                   }})

(defn is-item-from-source? [ source item ]
  (= source (get-in item [ :source :code ])))

(defn filter-source [ source items ]
  (filter
    #(is-item-from-source? source %) items))

(defn update-buffer-one-source [ source buffer items ]
  (update-in buffer [ source :data ]
    #(concat % (filter-source source items))))

(defn update-buffer [ buffer items ]
  (let [
        update-all-sources-fn
          (apply comp
            (map
              #(fn [b] (update-buffer-one-source % b items))
              (keys buffer)))
       ]
  (update-all-sources-fn buffer)))

(defn save-data [ fname data ]
  (spit fname (cc/generate-string data {:pretty true})))

(defn save-buffer-dump-one-source [ n source buffer ]
  (if (<= (count (get-in buffer [ source :data ])) n)
    buffer
    (let [
          [ dropper-f taker-f]
            (if (= n 0)
              [ (fn [seq] []) identity ]
              [  #(drop n %) #(take n %) ])
          buffer*
             (-> buffer
                (update-in [ source :data ] dropper-f)
                (update-in [ source :num-dumps ] inc))
          fname
             (format
               (get-in buffer [ source :out-fname-format ])
               (get-in buffer* [ source :num-dumps ]))
          _
            (save-data
               fname
               (taker-f
                 (get-in buffer [ source :data ])))
          ]
      (recur n source buffer*))))

(defn save-buffer-overhead [ buffer ]
  (let [
        save-all-sources-fn
          (apply comp
            (map
              #(fn [b]
                 (save-buffer-dump-one-source 200 % b))
              (keys buffer)))
       ]
    (save-all-sources-fn buffer)))

(defn save-buffer-rest [buffer]
    (let [
        save-all-sources-fn
          (apply comp
            (map
              #(fn [b]
                 (save-buffer-dump-one-source 0 % b))
              (keys buffer)))
       ]
    (save-all-sources-fn buffer)))

(defn handle-buffer [ extend-item-f [buffer url error] ]
  (let [
          [items url-next error]
            (fetch-items url)
          items*
            (map extend-item-f items)
           buffer*
             (if (and url-next (not error))
               (save-buffer-overhead
                 (update-buffer buffer items*))
               (save-buffer-rest
                 (save-buffer-overhead
                   (update-buffer buffer items*))))
        ]
  [ buffer* url-next error ]))

(defn expand-division-id [ division-id->cc-division
                           division-id->sc-division
                           judgment]
  (case (:courtType judgment)
    "COMMON"
       (update-in
         judgment
         [:division]
         #(division-id->cc-division (:id %)))
    "SUPREME"
       (update-in
         judgment
         [:division]
         #(division-id->sc-division (:id %)))
     judgment))

(defn fetch-buffer-all [ url  division-id->cc-division
                              division-id->sc-division ]
  (let [
         handle-buffer-f
          (partial handle-buffer
             (partial expand-division-id
                division-id->cc-division
                division-id->sc-division))
        ]
  (last
    (take-while
      is-there-more?
      (iterate handle-buffer-f [ empty-buffer url nil ])))
  nil))

;; COURTS and DIVISION

;; Generate map division-id->cc-division

(defn gen-division-id->cc-division-items [ court ]
  (let [
         empty-court
           (dissoc court :divisions)
         divisions
           (:divisions court)
         create-division-item-fn
           #(vector (:id %) (assoc % :court empty-court))
       ]
    (map create-division-item-fn divisions)))

(defn gen-division-id->cc-division-map [ courts ]
  (into
    {}
    (mapcat gen-division-id->cc-division-items courts)))

;; Generate map division-id->sc-division

(defn gen-division-id->sc-division-items [ chamber ]
  (let [
         empty-chamber
           (dissoc chamber :divisions)
         divisions
           (:divisions chamber)
         create-division-item-fn
           #(vector (:id %) (assoc % :chamber empty-chamber))
       ]
    (map create-division-item-fn divisions)))

(defn gen-division-id->sc-division-map [ chambers ]
  (into
    {}
    (mapcat gen-division-id->sc-division-items chambers)))
