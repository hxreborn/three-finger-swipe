<h1 align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/hxreborn/three-finger-swipe/main/.github/assets/3fs_white.svg">
    <img src="https://raw.githubusercontent.com/hxreborn/three-finger-swipe/main/.github/assets/3fs_black.svg" width="96" alt="">
  </picture>
  <br>
  Three Finger Swipe
</h1>

<p align="center">
  Xposed module that adds a configurable three-finger swipe-down gesture for screenshots and system actions.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-9%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 9+">
  <img src="https://img.shields.io/badge/libxposed-API_101%2B-ff69b4?style=for-the-badge" alt="libxposed API 101+">
</p>

## Features

- Selectable actions:
    - Take a screenshot
    - Turn the screen off
    - Toggle the flashlight
    - Change the ringer mode
    - Toggle split screen
- Adjustable swipe distance and edge exclusion
- Configurable finger landing window and trigger cooldown
- System API and SYSRQ screenshot methods for ROM compatibility

Changes apply immediately except the screenshot method, which requires a reboot.

## Requirements

- Android 9 through 16 (API 28–36)
- Xposed framework with libxposed API 101+ support, such as LSPosed or Vector

## Installation

1. Download the latest APK:

    <a href="../../releases"><img src="https://github.com/user-attachments/assets/d18f850c-e4d2-4e00-8b03-3b0e87e90954" height="45" alt="Get it on GitHub" /></a>
    <a href="http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22eu.hxreborn.tfs%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fhxreborn%2Fthree-finger-swipe%22%2C%22author%22%3A%22rafareborn%22%2C%22name%22%3A%22Three%20Finger%20Swipe%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%7D%22%7D"><img src="https://github.com/user-attachments/assets/dffe8fb9-c0d1-470b-8d69-6d5b38a8aa2d" height="45" alt="Get it on Obtainium" /></a>

2. Enable the module in your Xposed manager and scope it to System Framework (`system`)
3. Reboot your device
4. Open the app to configure the gesture

## FAQ

#### Why does this not conflict with CaptureSposed or DisableFlagSecure?

This module only hooks `PhoneWindowManager` in `system_server`. CaptureSposed and DisableFlagSecure
use different code paths, so their secure-window bypass behavior continues to work with this
module's screenshot gesture.

## Build

You need JDK 21 and the Android SDK.

```bash
git clone https://github.com/hxreborn/three-finger-swipe.git
cd three-finger-swipe
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

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.
For bugs or feature requests, [open an issue](https://github.com/hxreborn/three-finger-swipe/issues/new/choose).

## License

<a href="LICENSE"><img src="https://github.com/user-attachments/assets/b211cf0d-e255-421c-9213-6b6258676013" height="90" alt="GPLv3"></a>

This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.
