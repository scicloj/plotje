#!/usr/bin/env bash
cd "$(dirname "$0")"
clojure -M:dev:test -m cognitect.test-runner
