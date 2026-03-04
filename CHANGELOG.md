# Changelog


### Added

- Implement screen off, flashlight, and ringer mode actions
- Initial scaffold for more features by @hxreborn
- Add About screen with Material You header and Licenses screen by @hxreborn
- Replace single settings screen with multi-screen navigation by @hxreborn
- Add step snapping and state restore support by @hxreborn
- Add gesture tuning and capture mode settings by @hxreborn
- Add gesture tuning and capture mode pref specs by @hxreborn
- Add CaptureMode enum by @hxreborn
- Add app icons and splash animation by @hxreborn
- Add settings screen and companion app by @hxreborn
- Add Material 3 Expressive theme by @hxreborn
- Add preference system with remote bridge by @hxreborn
- Add initial crop screenshot capture and delivery by @hxreborn
- Add screenshot trigger and dispatch resolver by @hxreborn
- Add three-finger swipe state machine by @hxreborn
- Add Xposed entry point and system_server hook by @hxreborn

### Fixed

- Drop custom collect task, use plugin-registered exportLibraryDefinitions by @hxreborn
- Simplify aboutLibraries release variant task by @hxreborn
- Make action picker dialog scrollable by @hxreborn
- Replace swipe toggle with inline action picker dialog by @hxreborn
- Derive module status from XposedService binding by @hxreborn

### Changed

- Remove stub actions and unused action IDs
- Source GestureConfig defaults from Prefs
- Align generic action naming
- Rename CaptureMode.REFLECTION to SYSTEM_API
- Replace null checks with idiomatic Kotlin scope functions
- Rename ThreeFingerSwipeHandler to GestureHandler by @hxreborn
- Remove SWIPE_ENABLED, use NO_ACTION to disable gesture by @hxreborn
- Trim screenshot dispatch helpers by @hxreborn
- Enrich SYSRQ dispatch logs by @hxreborn
- Replace per-pref setters with generic savePref by @hxreborn
- Integrate CaptureMode and GestureConfig into hook chain by @hxreborn
- Cache swipe threshold and use readOrDefault by @hxreborn
- Simplify screenshot dispatch with multi-path resolution by @hxreborn
- Add findAllMethodsUpward and Method.signature helpers by @hxreborn
- Expand PrefsState and extract pushToRemote by @hxreborn
- Add IntPref range validation and readOrDefault extension by @hxreborn
- Simplify gesture illustration by @hxreborn
- Remove crop settings by @hxreborn
- Remove crop prefs by @hxreborn
- Drop crop gateway binding by @hxreborn
- Clean up gesture handler by @hxreborn
- Simplify screenshot trigger by @hxreborn
- Simplify dispatch resolver by @hxreborn
- Remove display capture gateway by @hxreborn
- Remove screenshot delivery by @hxreborn
- Remove crop capture core by @hxreborn
