#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:?Usage: release.sh <version>}"
VERSION="${VERSION#v}"

if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.]+)?$ ]]; then
  echo "Error: '$VERSION' is not valid semver"
  exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$BRANCH" != "main" ]]; then
  echo "Error: must be on main branch (currently on '$BRANCH')"
  exit 1
fi

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Error: working tree is not clean"
  exit 1
fi

git fetch origin

if ! command -v git-cliff &>/dev/null; then
  echo "Error: git-cliff is not installed (cargo install git-cliff)"
  exit 1
fi

TAG="v$VERSION"

MAJOR=$(echo "$VERSION" | cut -d. -f1)
COMMIT_COUNT=$(git rev-list --count HEAD)
VERSION_CODE=$(( MAJOR * 10000 + COMMIT_COUNT + 1 ))

echo "Version: $VERSION"
echo "Version code: $VERSION_CODE"
echo "Tag: $TAG"

FASTLANE_FILE="fastlane/metadata/android/en-US/changelogs/${VERSION_CODE}.txt"
if [[ ! -f "$FASTLANE_FILE" ]]; then
  echo "Warning: fastlane changelog not found at $FASTLANE_FILE"
  read -rp "Continue without fastlane changelog? [y/N] " REPLY
  [[ "$REPLY" =~ ^[Yy]$ ]] || exit 1
fi

git cliff --tag "$TAG" --config .github/cliff.toml -o CHANGELOG.md

sed -i "s/^version\.code=.*/version.code=$VERSION_CODE/" gradle.properties
sed -i "s/^version\.name=.*/version.name=$VERSION/" gradle.properties

echo ""
echo "Changes to commit:"
git diff --stat
echo ""
read -rp "Commit, tag, and push? [y/N] " REPLY
[[ "$REPLY" =~ ^[Yy]$ ]] || exit 1

git add -A
git commit -m "chore: release v$VERSION"
git tag -a "$TAG" -m "v$VERSION"
git push origin main "$TAG"

echo "Release $TAG pushed. CI will handle the rest."
