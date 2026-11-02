#!/usr/bin/env bash
# The suite's namespaces are named in build.clj (`test-namespaces`).
cd "$(dirname "$0")"
clojure -T:build run-tests
