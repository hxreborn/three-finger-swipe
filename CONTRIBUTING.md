# Contributing

## Build setup

```bash
git clone https://github.com/hxreborn/three-finger-swipe.git
cd three-finger-swipe
./gradlew assembleDebug
```

Requires JDK 21 (Temurin). See `gradle/libs.versions.toml` for all dependency versions.

## Code style

KtLint runs automatically on every build (`preBuild` depends on `ktlintCheck`). Fix violations
before committing:

```bash
./gradlew ktlintFormat
```

Rules are defined in `.editorconfig`. Key settings: 100 char line limit for Kotlin, 140 for Compose
UI files, trailing commas allowed.

## Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):

```
<type>(<scope>): <description>
```

Types: `feat`, `fix`, `refactor`, `perf`, `test`, `build`, `ci`, `docs`, `style`, `chore`
Scopes: `hooks`, `ui`, `module`, `gradle`, `ci`, `prefs`, `view`, `theme`

Subject line in imperative mood, max 50 chars, no period. Body uses bullet lines only if needed.

## Pull requests

1. Fork the repo and create a branch from `main`
2. Make your changes, ensure `./gradlew assembleDebug` and `./gradlew ktlintCheck` pass
3. Write a clear PR title following the commit format above
4. Keep PRs focused on a single change

## License

By contributing, you agree that your contributions will be licensed under the GPLv3 license.
