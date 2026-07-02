(ns kqe.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [quad-store.core :as qs]
            [kqe.core :as kqe]))

(defn- fixture-db []
  (-> (qs/empty-db)
      (qs/assert-quad {:s "alice" :p "role" :o "admin"})
      (qs/assert-quad {:s "alice" :p "name" :o "Alice"})
      (qs/assert-quad {:s "bob" :p "role" :o "user"})
      (qs/assert-quad {:s "carol" :p "role" :o "admin"})))

(deftest bound-subject-routes-to-spo
  (let [db (fixture-db)]
    (is (= #{{:s "alice" :p "role" :o "admin"} {:s "alice" :p "name" :o "Alice"}}
           (kqe/query db ["alice" nil nil])))
    (is (= #{{:s "alice" :p "role" :o "admin"}}
           (kqe/query db ["alice" "role" nil])))
    (is (= #{} (kqe/query db ["alice" "role" "user"])))))

(deftest bound-predicate-only-routes-to-pso
  (let [db (fixture-db)]
    (is (= #{{:s "alice" :p "role" :o "admin"}
             {:s "bob" :p "role" :o "user"}
             {:s "carol" :p "role" :o "admin"}}
           (kqe/query db [nil "role" nil])))))

(deftest bound-predicate-and-object-routes-to-pos
  (let [db (fixture-db)]
    (is (= #{{:s "alice" :p "role" :o "admin"} {:s "carol" :p "role" :o "admin"}}
           (kqe/query db [nil "role" "admin"])))))

(deftest fully-unbound-scans-everything
  (let [db (fixture-db)]
    (testing "unbound query returns every quad"
      (is (= 4 (count (kqe/query db [nil nil nil])))))))
