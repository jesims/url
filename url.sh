#!/usr/bin/env bash
cd $(realpath $(dirname $0))
if [[ ! -f project.sh ]];then
	curl --silent -OL https://raw.githubusercontent.com/jesims/backpack/master/project.sh
fi
source project.sh
if [[ $? -ne 0 ]];then
	exit 1
fi

shadow-cljs () {
	lein with-profile +test trampoline run -m shadow.cljs.devtools.cli $@
}

## stop:
## Stops shadow-cljs
stop () {
	shadow-cljs stop &>/dev/null
}

## clean:
## Cleans up the compiled and generated sources
clean () {
	stop
	lein clean
	rm -rf .shadow-cljs/
}

_unit-test () {
	refresh=$1
	clean
	echo_message 'In the animal kingdom, the rule is, eat or be eaten.'
	if [[ "${refresh}" = true ]];then
		lein auto test ${@:2}
	else
		lein test
	fi
	abort_on_error 'Clojure tests failed'
}

## test:
## args: [-r]
## Runs the Clojure unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
test () {
	case $1 in
		-r)
			_unit-test true ${@:2};;
		*)
			_unit-test;;
	esac
}

## test-cljs:
## Runs the ClojureScript unit tests
test-cljs () {
	shadow-cljs compile node \
	&& node target/node/test.js
	abort_on_error 'node tests failed'
}

is-snapshot () {
	version=$(cat VERSION)
	[[ "$version" == *SNAPSHOT ]]
}

deploy () {
	if [[ -n "$CIRCLECI" ]];then
		lein with-profile install deploy clojars &>/dev/null
		abort_on_error
	else
		lein with-profile install deploy clojars
		abort_on_error
	fi
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot () {
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		version=$(cat VERSION)
		snapshot="$version-SNAPSHOT"
		echo ${snapshot} > VERSION
		echo_message "Snapshotting $snapshot"
		case $1 in
			-l)
				lein with-profile install install
				abort_on_error;;
			*)
				deploy;;
		esac
		echo "$version" > VERSION
	fi
}

## release:
## Pushes a release to Clojars
release () {
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

script-invoke $@
