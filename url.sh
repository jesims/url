#!/usr/bin/env bash
#shellcheck disable=2215
cd "$(realpath "$(dirname "$0")")" &&
source bindle/project.sh
if [ $? -ne 0 ];then
	exit 1
fi

shadow-cljs () {
	lein with-profile +test trampoline run -m shadow.cljs.devtools.cli "$@"
}

## clean:
## Cleans up the compiled and generated sources
clean () {
	lein clean
	rm -rf .shadow-cljs/
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
	if [[ -n "$CIRCLECI" ]];then
		lein with-profile install deploy clojars &>/dev/null
		abort-on-error
	else
		lein with-profile install deploy clojars
		abort-on-error
	fi
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot () {
	#FIXME use bindle
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		version=$(cat VERSION)
		snapshot="$version-SNAPSHOT"
		echo "${snapshot}" > VERSION
		echo_message "Snapshotting $snapshot"
		case $1 in
			-l)
				lein with-profile install install
				abort-on-error;;
			*)
				deploy;;
		esac
		echo "$version" > VERSION
	fi
}

## release:
## Pushes a release to Clojars
release () {
	#FIXME use bindle
	version=$(cat VERSION)
	if ! is-snapshot;then
		version=$(cat VERSION)
		echo_message "Releasing $version"
		deploy
	else
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	fi
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
