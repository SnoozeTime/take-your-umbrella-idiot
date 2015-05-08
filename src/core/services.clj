(ns core.services
  (:require [core.weatherforecast :as wf])
  (:require [clj-time.core :as t]))

;; use java lib to send mails
(import 'org.apache.commons.mail.SimpleEmail)


(defn send-message [message]
  (doto (SimpleEmail.)
    (.setHostName "smtp.gmail.com")
    (.setSslSmtpPort "465")
    (.setSSL true)
    (.addTo "")
    (.setFrom "")
    (.setSubject "Take an umbrella")
    (.setMsg message)
    (.setAuthentication "...@gmail.com" "")
    (.send)))

;; other functions
(defn send-mail []
  (let [forecast (wf/forecast-tokyo)]
    (if (> (:precipProbability forecast) 0.30)
      (send-message (str "It is probably going to rain \n" forecast)))))
 
;; Is time between 7:30 and 7:40 ?
(defn time-between-interval? [[low-hour low-minute] [high-hour high-minute]]
  (let [time (t/to-time-zone (t/now) (t/time-zone-for-offset 9))
        hour (t/hour time)
        minute (t/minute time)]
    (cond
     (and (= hour low-hour high-hour) (<= minute high-minute) (>= minute low-minute)) true
     (and (= hour high-hour) (<= minute high-minute) (> hour low-hour)) true
     (and (= hour low-hour) (>= minute low-minute) (< hour high-hour)) true
     :else  false)))


;; the state of our services (running or not)
(def notice-run? (atom false))


(defn send-notice 
  "First draft - between 7:30 and 7:40, send me an email with the forecast"
  []
  ;; Have to run continuously...
  (loop [sent false]
    (cond
     (= false @notice-run?) '() ;; bye bye service
     (= false sent) (do
                      (Thread/sleep 10000)
                      (if (time-between-interval? [7 30] [7 40])
                        (do
                          ;; Fetch the forecast and send it
                          (send-mail)
                          (recur true))
                        (recur false))) 
     (= true sent) (do
                     (Thread/sleep 10000)
                     (if (time-between-interval? [9 0] [12 0])
                       (recur false)
                       (recur true))))))

;; Stop or start a service

(defn start-send-notice []
  (if (= true @notice-run?)
    false
    (do
      (reset! notice-run? true)
      (future (send-notice))
      true)))

(defn stop-send-notice []
  (reset! notice-run? false))
