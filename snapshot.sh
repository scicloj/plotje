#!/usr/bin/env bash
set -e

echo "Building SNAPSHOT..."
clojure -T:build ci :snapshot true

echo ""
echo "Deploying to Clojars..."
clojure -T:build deploy :snapshot true

echo ""
echo "Done!"
