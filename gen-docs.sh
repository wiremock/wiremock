#!/bin/bash

set -euo pipefail

pushd docs-v2
bundle exec jekyll build
cp -rf _site/* ../../wiremock-gh-pages/
popd

pushd ../wiremock-gh-pages
git add --all
git commit -m "Updated docs"
git push origin gh-pages
popd