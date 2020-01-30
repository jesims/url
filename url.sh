#!/usr/bin/env bash
#shellcheck disable=2215
cd "$(realpath "$(dirname "$0")")" &&
source bindle/project.sh
if [ $? -ne 0 ];then
	exit 1
fi

## clean:
## Cleans up the compiled and generated sources
clean () {
	lein-clean
}

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	-deps "$@"
}

## lint:
lint () {
	-lint
}

deploy () {
	deploy-clojars
}

deploy-snapshot () {
	deploy-clojars
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot () {
	-snapshot "$@"
}

## release:
## Pushes a release to Clojars
release () {
	-release
}

## test:
## args: [-r]
## Runs the Clojure unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
test () {
	-test-clj "$@"
}

## test-cljs:
## Runs the ClojureScript unit tests
test-cljs () {
	-test-cljs "$@"
}

script-invoke "$@"
