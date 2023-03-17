#!/usr/bin/env bash

set -euo pipefail

yarn serve &
rbenv exec bundle exec jekyll serve
