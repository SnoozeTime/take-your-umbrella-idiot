(ns core.weatherforecast
  (:require [clj-http.client :as client]) ;; To make get requests
  (:use [slingshot.slingshot])) ;; try+


;; Others 
(defn fahrenheit->celcius [fahrenheit]
  (float (* (/ 5 9) (- fahrenheit 32))))


(def api-key "2183cffc4c438e188") ;; insert here api key from forecast.io


;; HTTP CLIENT
;; uses forecast.io to get the forecast of the day
;; If there is an error in the request, it just give a empty object
(defn hourly-forecast 
  "Give a JSON object with the hourly weather forecast, given a longitude and a latitude"  
  [longitude latitude]
  (try+
   ;; clj-http give exceptions for extraordinary status code such as 404
   (let [req (client/get (str "https://api.forecast.io/forecast/" api-key "/" latitude "," longitude) {:as :json})]
     ;;Check weather the request is successful
     (if (= 200 (:status req))
       ;;return the hourly forecast
       (get-in req [:body :hourly])
       '()))
   ;;Something went wrong with the request
   (catch Object _ 
     '())))

;; Parse the data from our hourly-forecast
;; interesting data here - temperature, precipProbability, precipIntensity, humidity, windSpeed
(def interesting-keys [:temperature
                       :precipProbability
                       :precipProbability
                       :humidity
                       :windSpeed])

(defn interesting-data [data keys]
  (map (fn [x]
         (select-keys x keys)) 
       data))


;; from a seq of number, return the mean
(defn mean [a-seq]
  (let [cnt (count a-seq)]
    (if (= 0 cnt)
      0
      (let [sum (apply + a-seq)]
        (/ sum cnt)))))

;; Check the result of the get request to understand the data manipulation
(defn key->mean [a-seq key]
  (let [b-seq (filter identity (map #(get % key) a-seq))]
    ;;special case for temperature, we have to convert to celcius (because we are civilised)
    (if (= key :temperature)
      (fahrenheit->celcius (mean b-seq))
      (mean b-seq))))

;; All the mean for the interesting keys
(defn data-means [data keys]
  (if (= 0 (count keys))
    {}
    (let [m (key->mean data (first keys))]
      (conj {(first keys) m} (data-means data (rest keys))))))

;; How to use it
;; hourly is the output of hourly-forecast
;; returns a map with interesting-keys and the mean associated to these keys
(defn parsed-data [hourly]
  (let [data (interesting-data (:data hourly) interesting-keys)]
    (data-means data interesting-keys)))

(defn forecast-tokyo []
  (let [hourly (hourly-forecast 139.6833 35.6833)]
    (parsed-data hourly)))
