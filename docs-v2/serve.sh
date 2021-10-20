#!/usr/bin/env bash

set -euo pipefail

npm run serve &
bundle exec jekyll serve