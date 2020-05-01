# 0.3.0

Added:

* Custom Encoder Support. Thanks @saitouena. #4

Miscellaneous:

* Update `io.jesi/parent` to `3.9.0`
* Update `lein-parent` to `0.3.8`
* Update `ws` to `7.2.5`
* Moved `thheller/shadow-cljs` to dev-dependency

# 0.2.1

* Fix cljsbuild issue with multiple requires in ns

# 0.2.0

Added:

* Anchors are now broken into `anchor` and `anchor-query`

Changed:

* Upgraded clojure to `1.10.1`

# 0.1.2

Added:

* Bumped Clojure dependency to `1.9.0`
* `query->map` and `map->query` to support repeating param identifiers

Changed:

* Changed to `CLJC` instead of `CLJX`
* Use `Shadow-cljs` to compile and test ClojureScript in NodeJS
* Added helper BASH automation scripts in `url.sh`
