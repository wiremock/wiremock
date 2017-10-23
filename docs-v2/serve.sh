#!/usr/bin/env bash

set -euo pipefail

npm run watch:all &
bundle exec jekyll serve