#!/bin/bash

set -euo pipefail

npm i -g npm-run-all swagger-cli
npm i
bundle install

npm run build:all
bundle exec jekyll build
