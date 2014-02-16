(ns tailrecursion.boot.task.ring
  (:require
   [tailrecursion.boot.core        :as core]
   [ring.adapter.jetty             :as jetty]
   [ring.middleware.session        :as session]
   [ring.middleware.session.cookie :as cookie]
   [ring.middleware.head           :as head]
   [ring.middleware.file           :as file]
   [ring.middleware.file-info      :as file-info]))

(def server     (atom nil))
(def middleware (atom identity))

(defn handle-404
  [req]
  {:status 404 :headers {} :body "Not Found :("})

(core/deftask files
  [& [docroot]]
  (let [docroot (or docroot (core/get-env :out-path))
        this-mw #(-> %
                   (file/wrap-file docroot)
                   (file-info/wrap-file-info))]
    (swap! middleware comp this-mw)
    identity))

(core/deftask head
  []
  (swap! middleware comp head/wrap-head)
  identity)

(core/deftask session-cookie
  [& [key]]
  (let [dfl-key "a 16-byte secret"
        store   (cookie/cookie-store {:key (or key dfl-key)})]
    (swap! middleware comp #(session/wrap-session % {:store store}))
    identity))

(core/deftask dev-mode
  []
  (let [set-dev #(assoc % "X-Dev-Mode" "true")
        add-hdr #(update-in % [:headers] set-dev)]
    (swap! middleware comp #(comp add-hdr %))
    identity))

(core/deftask jetty
  [& {:keys [port join?] :or {port 8000 join? false}}]
  (println
    "Jetty server stored in atom here: #'tailrecursion.boot.task.ring/server...")
  (core/with-pre-wrap
    (swap! server
      #(or % (-> (@middleware handle-404)
               (jetty/run-jetty {:port port :join? join?}))))))

(core/deftask dev-server
  []
  (comp (head) (dev-mode) (session-cookie) (files) (jetty)))
