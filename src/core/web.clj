(ns core.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [core.services :as services]))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (pr-str [(str "Hello " )])})

(defroutes app
  (GET "/" [] ;; Just add your email address and time of weather report to the DB
       (splash))
  (GET "/unsubscribe/:email" [email]
       (str email))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))


(defn -main [& [port]]
  (do
    (services/start-send-notice)
    (let [port (Integer. (or port (env :port) 5000))]
      (jetty/run-jetty (site #'app) {:port port :join? false}))))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
