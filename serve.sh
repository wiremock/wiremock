#!/usr/bin/env bash

set -euo pipefail

npm run serve &
rbenv exec bundle exec jekyll serve
