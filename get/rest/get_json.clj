(require '[cheshire.core :as cc])
(require '[clj.rest-get :as get])
(require '[clj.common :as cljc])

(def SAOS-API-DUMP-URL "https://saos-test.icm.edu.pl/api/dump/")

(defn create-expand-id-function
  [ division-id->cc-division division-id->sc-division chamber-id->sc-chamber]
  (fn [ judgment ]
    (case (:courtType judgment)
      "COMMON"
         (update-in
           judgment
           [:division]
           #(division-id->cc-division (:id %)))
      "SUPREME"
         (-> judgment
           (update-in
             [:division]
             #(division-id->sc-division (:id %)))
           (update-in
             [:chambers]
             (fn [chambers]
               (map
                 #(chamber-id->sc-chamber (:id %))
                 chambers))))
       judgment)))

(def court-type->out-fname-format
  { "COMMON"  "out/commo_court_%04d.json.gz"
    "SUPREME" "out/supre_court_%04d.json.gz"
    "CONSTITUTIONAL_TRIBUNAL" "out/const_tribu_%04d.json.gz"
    "NATIONAL_APPEAL_CHAMBER" "out/appea_chamb_%04d.json.gz"})

(defn report-error [ error ]
  (when error
    (println (str "Error - " error))))

(defn run [ argv ]
  (let [
         { :keys [ getURL ] }
           (cljc/read-properties (first argv))
         [ division-id->cc-division error-common-court-divs ]
           (get/fetch-common-court-divisions getURL)
	 _ (report-error error-common-court-divs)
         [division-id->sc-division chamber-id->sc-chamber error-supreme-court-divs]
           (get/fetch-supreme-court-divisions getURL)
         _ (report-error error-supreme-court-divs)
         transform-f
           (create-expand-id-function
             division-id->cc-division division-id->sc-division chamber-id->sc-chamber)
         error
	   (if-not (or error-common-court-divs error-supreme-court-divs)
             (get/fetch-buffer-all
                (str getURL
                  "judgments?pageSize=100&pageNumber=0&withGenerated=false")
                  court-type->out-fname-format
                  transform-f)
	     (println "Skipping fetching phase because of the previous errors."))
         _ (report-error error)
        ]
    (if-not (or error-common-court-divs error-supreme-court-divs error)
      0
      1)))

(when (> (count *command-line-args*) 0)
  (System/exit
   (run  *command-line-args*)))
