# kqe (retired)

**This repo is retired.** Its content (the Kotoba Query Engine —
`[s p o]` triple-pattern query over a `quad-store.core` db) has been
merged into [`kotoba-lang/arrangement`](https://github.com/kotoba-lang/arrangement)
as the `arrangement.query` namespace, alongside the storage repo it
always routed over (`quad-store`, itself renamed to `arrangement` in the
same change — Datomic's own term for the 4-covering-index structure it
stores).

A pure routing function with no storage of its own didn't need a
separate repo. See ADR-2607050600.

No further commits will land here. Zero other repos in the org depended
on this one directly (only `kotoba-lang/kotobase-peer`, formerly
`kotobase-engine`, did — it now depends on `arrangement` instead).
