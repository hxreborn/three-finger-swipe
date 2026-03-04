# Three Finger Swipe

Standalone implementation of three-finger swipe screenshots using the modern LSPosed API
<p align="center">
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-9%E2%80%9316-3DDC84?style=flat&logo=android&logoColor=white" alt="Android 9-16" /></a>
  <a href="https://github.com/LSPosed/LSPosed"><img src="https://img.shields.io/badge/LSPosed_API-100-8F00FF?style=flat" alt="LSPosed API 100" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/compose"><img src="https://img.shields.io/badge/Compose-Material_3_Expressive-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://gradle.org"><img src="https://img.shields.io/badge/Gradle-8.12-02303A?style=flat&logo=gradle&logoColor=white" alt="Gradle" /></a>
  <a href="https://developer.android.com/build"><img src="https://img.shields.io/badge/AGP-8.10-02303A?style=flat&logo=android&logoColor=white" alt="AGP" /></a>
</p>

<p align="center">
  <a href="https://github.com/hxreborn/three-finger-swipe/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/hxreborn/three-finger-swipe/build.yml?label=build&style=flat&logo=githubactions&logoColor=white" alt="Build" /></a>
  <a href="https://github.com/hxreborn/three-finger-swipe/releases/latest"><img src="https://img.shields.io/github/v/release/hxreborn/three-finger-swipe?style=flat&logo=github" alt="Release" /></a>
  <a href="https://github.com/hxreborn/three-finger-swipe/releases"><img src="https://img.shields.io/github/downloads/hxreborn/three-finger-swipe/total?style=flat&logo=github" alt="Downloads" /></a>
  <a href="https://github.com/hxreborn/three-finger-swipe/blob/main/LICENSE"><img src="https://img.shields.io/github/license/hxreborn/three-finger-swipe?style=flat" alt="License" /></a>
</p>

## Features

- Three-finger swipe down to take a screenshot (or trigger other actions)
- Single-purpose module with one hook on System Framework
- Compatible with [CaptureSposed](https://github.com/99keshav99/CaptureSposed) and [DisableFlagSecure](https://github.com/Xposed-Modules-Repo/io.github.lsposed.disableflagsecure)
- Settings UI built with Jetpack Compose and Material 3 Expressive
- Built on LSPosed API 100
- Free and open source (GPLv3)

## Requirements

- Android 9 through 16 (API 28-36)
- [LSPosed](https://github.com/JingMatrix/LSPosed) (JingMatrix fork recommended)

## Installation

1. Download the latest APK:

    <a href="../../releases"><img src="https://github.com/user-attachments/assets/d18f850c-e4d2-4e00-8b03-3b0e87e90954" height="45" alt="Get it on GitHub" /></a>
    <a href="http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22eu.hxreborn.tfs%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fhxreborn%2Fthree-finger-swipe%22%2C%22author%22%3A%22rafareborn%22%2C%22name%22%3A%22Three%20Finger%20Swipe%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%7D%22%7D"><img src="https://github.com/user-attachments/assets/dffe8fb9-c0d1-470b-8d69-6d5b38a8aa2d" height="45" alt="Get it on Obtainium" /></a>

2. Enable the module in LSPosed and scope it to `system`
3. Reboot your device
4. Open the app to configure gesture settings

## FAQ

#### Why does this not conflict with CaptureSposed or DisableFlagSecure?

This module only hooks `PhoneWindowManager` in `system_server`. CaptureSposed and DisableFlagSecure work through different code paths, so you keep their secure-window bypass behavior while using this module's three-finger gesture to trigger the screenshot.

## Build

You need JDK 21 and the Android SDK.

```bash
git clone --recurse-submodules https://github.com/hxreborn/three-finger-swipe.git
cd three-finger-swipe

# Build libxposed dependencies (first time only)
./gradlew buildLibxposed

# Build debug APK
./gradlew :app:assembleDebug
```

<details>
<summary>Release signing</summary>

Add to `local.properties`:

```properties
RELEASE_STORE_FILE=<path/to/keystore.p12>
RELEASE_STORE_PASSWORD=<store_password>
RELEASE_KEY_ALIAS=<key_alias>
RELEASE_KEY_PASSWORD=<key_password>
```

Then build with `./gradlew :app:assembleRelease`.

</details>

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.
For bugs or feature requests, [open an issue](https://github.com/hxreborn/three-finger-swipe/issues/new/choose).

## License

<a href="LICENSE"><img src="https://github.com/user-attachments/assets/b211cf0d-e255-421c-9213-6b6258676013" height="90" alt="GPLv3"></a>

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
