#!/bin/bash

set -euo pipefail

yarn
rbenv exec bundle install

yarn build:all
rbenv exec bundle exec jekyll build
