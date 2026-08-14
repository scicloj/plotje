#!/usr/bin/env bash
set -e

echo "Building..."
clojure -T:build ci

echo ""
echo "Deploying to Clojars..."
clojure -T:build deploy

echo ""
echo "Done!"
