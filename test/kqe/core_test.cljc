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

;; ── query-time visibility seam (ADR-2607050500) ─────────────────────────────

(deftest visible-defaults-to-permissive
  (let [db (fixture-db)]
    (is (= (kqe/query db [nil "role" nil])
           (kqe/query db [nil "role" nil] (constantly true)))
        "omitting visible? behaves identically to a permissive predicate")))

(deftest visible-redacts-without-kqe-knowing-why
  (let [db (fixture-db)
        admins-only? (fn [{:keys [s]}] (not= "bob" s))]
    (testing "a composing layer can hide specific quads from a result"
      (is (= #{{:s "alice" :p "role" :o "admin"} {:s "carol" :p "role" :o "admin"}}
             (kqe/query db [nil "role" nil] admins-only?))))
    (testing "visible? applies uniformly across every routing branch"
      (is (= #{{:s "alice" :p "role" :o "admin"} {:s "alice" :p "name" :o "Alice"}}
             (kqe/query db ["alice" nil nil] admins-only?)))
      (is (= #{{:s "carol" :p "role" :o "admin"}}
             (kqe/query db [nil "role" "admin"] (fn [{:keys [s]}] (= s "carol")))))
      (is (= 3 (count (kqe/query db [nil nil nil] admins-only?)))))
    (testing "an always-false visible? redacts everything"
      (is (= #{} (kqe/query db [nil nil nil] (constantly false)))))))
