#!/bin/bash

set -euo pipefail

rbenv exec bundle exec jekyll build
cp -rf _site/* ../wiremock-gh-pages/

pushd ../wiremock-gh-pages
git add --all
git commit -m "Updated docs"
git push origin gh-pages
popd
