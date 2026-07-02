# kqe

`kotoba-lang/kqe` is the shared CLJC home for the Kotoba Query Engine
(same name as the deleted `kotoba-query` Rust crate) — `[s p o]`
triple-pattern query with wildcards over a `quad-store.core` db. See
`90-docs/adr/2607010930-clj-wgsl-migration.md` Phase 6.

Routing mirrors the deleted `route_bgp_triples`: bound subject routes to
`spo`, bound-predicate-only routes to `pso`, bound predicate + bound object
routes to `pos`, fully unbound scans `spo` in full.

## Use

```clojure
(require '[quad-store.core :as qs] '[kqe.core :as kqe])

(def db (-> (qs/empty-db)
            (qs/assert-quad {:s "alice" :p "role" :o "admin"})
            (qs/assert-quad {:s "bob" :p "role" :o "user"})))

(kqe/query db ["alice" nil nil])   ;=> #{{:s "alice" :p "role" :o "admin"}}
(kqe/query db [nil "role" "admin"]) ;=> #{{:s "alice" :p "role" :o "admin"}}
```

## Scope of this landing

Pattern query over the **hot** (in-memory) db only, with index routing.
**Not in this landing** (tracked follow-ups, not silently omitted):

- Cold (prolly-tree-backed, post-`commit!`) query.
- Full Datalog semi-naive fixpoint evaluation (recursive rules, transitive
  closure).
- SPARQL-style BGP (multi-triple join, FILTER/OPTIONAL/UNION).

## Test

```bash
clojure -M:test
```

## License

MIT
