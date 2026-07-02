(ns kqe.core
  "Kotoba Query Engine (name matches the deleted kotoba-query Rust crate) --
  `[s p o]` triple-pattern query with wildcards (nil) over a
  `quad-store.core` db, routing to whichever index matches the bound
  positions. Mirrors the deleted `route_bgp_triples` routing: bound subject
  -> spo, bound predicate only -> pso, bound predicate + bound object ->
  pos, fully unbound -> full spo scan.

  Only the hot (in-memory) `quad-store.core` db is queried here. Cold
  (prolly-tree-backed, post-`commit!`) query and full Datalog
  fixpoint/SPARQL BGP evaluation are explicitly NOT in this landing --
  tracked as follow-ups, not silently omitted."
  (:require [quad-store.core :as qs]))

(defn query
  "`pattern` is `[s p o]`, any position `nil` for wildcard. Returns a set of
  matching `{:s :p :o}` quads."
  [db [s p o]]
  (cond
    (some? s)
    (into #{}
          (for [[p2 os] (qs/entity-attrs db s)
                :when (or (nil? p) (= p p2))
                o2 os
                :when (or (nil? o) (= o o2))]
            {:s s :p p2 :o o2}))

    (and (some? p) (some? o))
    (into #{} (for [s2 (qs/by-predicate-value db p o)] {:s s2 :p p :o o}))

    (some? p)
    (into #{} (for [[s2 os] (qs/by-predicate db p) o2 os] {:s s2 :p p :o o2}))

    :else
    (into #{} (for [[s2 pm] (:spo db) [p2 os] pm o2 os] {:s s2 :p p2 :o o2}))))
