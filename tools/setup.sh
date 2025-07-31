#!/usr/bin/env sh

printf '%s' "Your name: "
read USER_NAME

set -x

git config user.name "$USER_NAME"
git config core.hooksPath .githooks