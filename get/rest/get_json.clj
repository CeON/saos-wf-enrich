(require '[cheshire.core :as cc])
(require '[clj.rest-get :as get])

(def court-type->out-fname-format
  { "COMMON"  "out/commo_court_%04d.json.gz"
    "SUPREME" "out/supre_court_%04d.json.gz"
    "CONSTITUTIONAL_TRIBUNAL" "out/const_tribu_%04d.json.gz"
    "NATIONAL_APPEAL_CHAMBER" "out/appea_chamb_%04d.json.gz"})

(defn run []
  (get/fetch-buffer-all
    "https://saos-test.icm.edu.pl/api/dump/judgments?pageSize=100&pageNumber=0"
    court-type->out-fname-format))

(run)
