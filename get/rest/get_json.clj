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

(defn run [ argv ]
  (let [
         { :keys [ getURL ] }
           (cljc/read-properties (first argv))
         division-id->cc-division
           (get/fetch-common-court-divisions getURL)
         [division-id->sc-division chamber-id->sc-chamber]
           (get/fetch-supreme-court-divisions getURL)
         transform-f
           (create-expand-id-function
             division-id->cc-division division-id->sc-division chamber-id->sc-chamber)
        ]
  (get/fetch-buffer-all
    (str getURL
       "judgments?pageSize=100&pageNumber=0&withGenerated=false")
    court-type->out-fname-format
    transform-f)))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
