(set-env!
  :dependencies '[[adzerk/boot-cljs          "1.7.228-1"]
          [adzerk/boot-reload        "0.4.8"]
          [hoplon/boot-hoplon        "0.1.13"]
          [hoplon/hoplon             "6.0.0-alpha15"]
          [org.clojure/clojure       "1.7.0"]
          [org.clojure/clojurescript "1.9.36"]
          [tailrecursion/boot-jetty  "0.1.3"]]
  :source-paths #{"src"}
  :asset-paths  #{"assets"})

(require
  '[clojure.string :refer [split]]
  '[adzerk.boot-cljs         :refer [cljs]]
  '[adzerk.boot-reload       :refer [reload]]
  '[hoplon.boot-hoplon       :refer [hoplon prerender]]
  '[tailrecursion.boot-jetty :refer [serve]])

(deftask diff
  "Call next handler when file contents differ from before"
  []
  (let [prev-ids (atom (vec []))]
    (fn [next-task]
      (fn [fileset]
        (let [remove-period (fn [ids] (-> ids (split #"\.") (get 0)))
            ids (->> fileset ls (map :id) vec)]
          (when (not= (set (map remove-period ids)) (set (map remove-period @prev-ids)))
            (next-task fileset))
          (reset! prev-ids ids))))))

(deftask dev
  "Build for local development."
  []
  (comp
    (watch)
    (diff)
    (notify)
    (hoplon)
    (reload)
    (cljs)
    (serve :port 8000 :init-params {"org.eclipse.jetty.servlet.Default.useFileMappedBuffer" "false"})))

(deftask prod
  "Build for production deployment."
  []
  (comp
    (hoplon)
    (cljs :optimizations :advanced)
    (target :dir #{"target"})))
