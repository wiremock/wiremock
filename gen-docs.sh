#!/bin/bash

pushd docs
make html
popd

cp -r docs/build/html/* ../wiremock-gh-pages

pushd ../wiremock-gh-pages
git add --all
git commit -m "Updated docs"
git push origin gh-pages
popd