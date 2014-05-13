(ns tailrecursion.boot.task.ring
  (:require
   [clojure.java.io                :as io]
   [tailrecursion.boot.core        :as core]
   [ring.adapter.jetty             :as jetty]
   [ring.middleware.cors           :as cors]
   [ring.middleware.session        :as session]
   [ring.middleware.session.cookie :as cookie]
   [ring.middleware.reload         :as reload]
   [ring.middleware.head           :as head]
   [ring.middleware.file           :as file]
   [ring.middleware.file-info      :as file-info]))

(def server     (atom nil))
(def middleware (atom identity))

(defn ring-task [mw]
  (swap! middleware comp mw)
  identity)

(defn handle-404
  [req]
  {:status 404 :headers {} :body "Not Found :("})

(core/deftask cors
  "Ring task to support cross-origin requests.

  allowed-origins is a list of regular expressions matching the permitted 
  origin(s) or a single function which takes the origin as its argument 
  to return a truthy value if it is to be allowed."
  [& allowed-origins]
  (ring-task #(apply cors/wrap-cors % allowed-origins)))

(core/deftask files
  "Ring task to serve static files.

  The document root can be specified via the optional `docroot` argument. If not
  specified the :out-path will be used."
  [& [docroot]]
  (let [root (or docroot (core/get-env :out-path))]
    (.mkdirs (io/file root))
    (ring-task #(-> (file/wrap-file % root) (file-info/wrap-file-info)))))

(core/deftask head
  "Ring task to handle HEAD requests."
  []
  (ring-task head/wrap-head))

(core/deftask session-cookie
  "Ring task to support client sessions via a session cookie.

  The optional `key` argument sets the cookie encryption key to the given 16
  character string."
  [& [key]]
  (let [dfl-key "a 16-byte secret"
        store   (cookie/cookie-store {:key (or key dfl-key)})]
    (ring-task #(session/wrap-session % {:store store}))))

(core/deftask dev-mode
  "Ring task to add the `X-Dev-Mode` header to all responses."
  []
  (let [set-dev #(assoc % "X-Dev-Mode" "true")
        add-hdr #(update-in % [:headers] set-dev)]
    (ring-task #(comp add-hdr %))))

(core/deftask reload
  "Ring task to support reloading of namespaces during development."
  []
  (ring-task #(reload/wrap-reload % {:dirs (vec (core/get-env :src-paths))})))

(core/deftask jetty
  "Ring task to start a local Jetty server.

  The `:port` option specifies which port the server should listen on (default
  8000). The `:join?` option specifies whether Jetty should run in the foreground
  (default false)."
  [& {:keys [port join?] :or {port 8000 join? false}}]
  (println
    "Jetty server stored in atom here: #'tailrecursion.boot.task.ring/server...")
  (core/with-pre-wrap
    (swap! server
      #(or % (-> (@middleware handle-404)
               (jetty/run-jetty {:port port :join? join?}))))))

(core/deftask dev-server
  "Ring task to start a local development stack.

  The optional `:port` and `:join?` options are passed to the `jetty` task,
  `:key` to the `session-cookie` task, and `:docroot` to the `files` task."
  [& {:keys [port join? key docroot]
      :or {port    8000
           join?   false
           key     "a 16-byte secret"
           docroot (core/get-env :out-path)}}]
  (comp (head) (dev-mode) (session-cookie key) (files docroot) (jetty :port port :join? join?)))
