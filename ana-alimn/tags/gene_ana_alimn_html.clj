(require
  '[clojure.string :as str]
  '[hiccup.page :as hp]
  '[clj.common :as com])

(defn conv-to-table-row [ alimony-tag ]
  (let [
         id (:judgmentId alimony-tag)
        ]
    [ :tr
      [ :td [ :a
              {:href (str "https://saos-test.icm.edu.pl/judgments/" id) }
              id ] ]
      [ :td (get-in alimony-tag [:value :reason]) ]
      [ :td (if (get-in alimony-tag [:value :isAppeal])
              "yes"
              "no")]
      [ :td (get-in alimony-tag [:value :defendantSex])]
      [ :td (get-in alimony-tag [:value :result])]]))

(defn conv-tags-to-html-table [ alimony-tags ]
  (let [
         out-list-items-html
           (map conv-to-table-row alimony-tags)
         table-html
           [ :table {:border "1" :width "100%" :id "tablesortertable" :class "tablesorter"}
             [ :thead
               [ :tr [:th "ID"] [:th "REASON"] [:th "IS APPEAL?"] [:th "DEFENDANT SEX"] [:th "RESULT"]]]
             [ :tbody
               out-list-items-html]]
       ]
    (hp/html5
       [:head
          [:meta {:http-equiv "content-type", :content "text/html;charset=utf-8"} ]
          [:script {:type "text/javascript" :src "../html/jquery-latest.js" } ]
          [:script {:type "text/javascript" :src "../html/__jquery.tablesorter.min.js" } ]
          [:script {:type "text/javascript" :src "../html/chili-1.8b.js"} ]
          [:link {:rel "stylesheet" :href "../html/jq.css" :type "text/css" :media "print, projection, screen"} ]
          [:link {:rel "stylesheet" :href "../html/style.css" :type "text/css" :media "print, projection, screen"} ]
          [:script {:type "text/javascript"}
              "$(function() {
                $('#tablesortertable').tablesorter({sortList:[[0,0],[2,1]], widgets: ['zebra']});
                $('#options').tablesorter({sortList: [[0,0]], headers: { 3:{sorter: false}, 4:{sorter: false}}});});" ]]
       [:body table-html])))

(defn run [argv]
  (let [
          inp-fname
            (first argv)
          out-fname
            (second argv)
          alimony-tags
            (com/read-json inp-fname)
          alimony-html
            (conv-tags-to-html-table alimony-tags)
       ]
    (print alimony-html)))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
