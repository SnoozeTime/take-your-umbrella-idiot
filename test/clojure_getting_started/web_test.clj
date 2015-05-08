(ns clojure-getting-started.web-test
  (:require [clojure.test :refer :all]
            [core.services :refer :all]))

(deftest first-test
  (testing "time between interval"
    (is (= true (time-between-interval? [7 30] [9 50])))
    (is (= true (time-between-interval? [9 39] [9 41])))
    (is (= false (time-between-interval? [8 1] [8 6])))
    (is (= false (time-between-interval? [9 50] [8 3])))))
