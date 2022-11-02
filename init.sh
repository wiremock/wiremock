#!/bin/bash

set -euo pipefail

npm i -g npm-run-all swagger-cli
npm i
rbenv exec bundle install

npm run build:all
rbenv exec bundle exec jekyll build
