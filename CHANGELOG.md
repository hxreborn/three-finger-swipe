
## What's Changed



### Features

- [`58a5357`](https://github.com/hxreborn/three-finger-swipe/commit/58a5357a112d4035c4e7c9223b4ed415458f851c) *(prefs)* Add a split screen method preference by @hxreborn


### Bug Fixes

- [`86b49d2`](https://github.com/hxreborn/three-finger-swipe/commit/86b49d29fb5038edf7c3dd68ad983b1f1a12f1f9) *(action)* Probe the split screen shell route lazily by @hxreborn

- [`f0acca1`](https://github.com/hxreborn/three-finger-swipe/commit/f0acca18cd020d62402ecfdecac945629ca30007) *(action)* Skip the shell split route for tasks WMShell cannot stage by @hxreborn

- [`19c16d9`](https://github.com/hxreborn/three-finger-swipe/commit/19c16d97a091d636dbfd626f37b8f8d32783e7f5) *(action)* Add a WMShell split screen route by @hxreborn

- [`489131d`](https://github.com/hxreborn/three-finger-swipe/commit/489131d9689c2da8bcf9f52a51caae9a796fe31e) *(ci)* Title mirror releases and derive the code-name tag on re-mirror by @hxreborn

- [`d3ac674`](https://github.com/hxreborn/three-finger-swipe/commit/d3ac67453f83687598ff1ba09c8ed8214fdcb849) *(gesture)* Dispose the input receiver on the main looper by @hxreborn



## What's Changed



### Features

- [`184217b`](https://github.com/hxreborn/three-finger-swipe/commit/184217be3f55648a6b7858858b316ee1d20c7dfb) *(action)* Add toggle split screen action per Android version by @hxreborn

- [`0db870b`](https://github.com/hxreborn/three-finger-swipe/commit/0db870b8bfbe3d60c9189b1f056b8442fe20a098) *(module)* Support hot reload and register one guarded prefs listener by @hxreborn

- [`6559585`](https://github.com/hxreborn/three-finger-swipe/commit/65595853314fd2dccecd9d9bf7cf29993a7df02f) *(ui)* Add the split screen action and drop the debug log toggle by @hxreborn


### Bug Fixes

- [`0cf9f80`](https://github.com/hxreborn/three-finger-swipe/commit/0cf9f80d67b042de9b703310c5fa5530689ffa27) *(ci)* Emit code-name mirror tag, drop duplicate auto-mirror by @hxreborn

- [`2b6035e`](https://github.com/hxreborn/three-finger-swipe/commit/2b6035e3629689569978230bf31a3b0042990e51) *(ui)* Add navigate-up content description and inline AboutCard slot by @hxreborn

- [`c61f416`](https://github.com/hxreborn/three-finger-swipe/commit/c61f416b018cb921aa09f1be24d01e825baa906f) *(ui)* Use removeAt instead of removeLast for minSdk 28 by @hxreborn


### Performance Improvements

- [`bc42156`](https://github.com/hxreborn/three-finger-swipe/commit/bc421563a6f61eb186ff91164cafd324bef19bec) *(hook)* Cache prefs in HookPrefs, drop per-event RemotePreferences IPC by @hxreborn

- [`feb7bd4`](https://github.com/hxreborn/three-finger-swipe/commit/feb7bd43134b9028d6d0df715cc7b120f6c65bed) *(ui)* Cache rounded shapes as top-level constants by @hxreborn


### Refactor

- [`ec8fc51`](https://github.com/hxreborn/three-finger-swipe/commit/ec8fc5122915814ef0b2892168abafd74b11ede3) *(action)* Resolve system services once and close held resources by @hxreborn

- [`510f5fb`](https://github.com/hxreborn/three-finger-swipe/commit/510f5fb0b05e8cfa8229436891173f4186a1a83a) *(hook)* Guard install and add teardown for the pointer listener by @hxreborn

- [`531a83b`](https://github.com/hxreborn/three-finger-swipe/commit/531a83bfd32799c3121146105894bb190c4b547d) *(hook)* Rename PhoneWindowManagerHooker to PhoneWindowManagerHook by @hxreborn

- [`a35053c`](https://github.com/hxreborn/three-finger-swipe/commit/a35053c0ffa6d034d0c8dbf7635f790c36f3786d) *(module)* Move prefs and service binding ownership to App by @hxreborn

- [`7022e92`](https://github.com/hxreborn/three-finger-swipe/commit/7022e9246e0796aef4bcaa3cdd1be2178018ca0a) *(module)* Route logging through one severity based facade by @hxreborn

- [`d4747ab`](https://github.com/hxreborn/three-finger-swipe/commit/d4747abd277f9ba3cb07a43bb798730f244337c2) *(prefs)* Pass remote prefs through a provider lambda by @hxreborn

- [`e55a94a`](https://github.com/hxreborn/three-finger-swipe/commit/e55a94a60709325f7a117a6469a7d33b8b0450a0) *(prefs)* Rename PrefsState to AppPrefs by @hxreborn

- [`514afb3`](https://github.com/hxreborn/three-finger-swipe/commit/514afb35484787130d6d5a5a7905e68da4005dba) *(ui)* Drop fillMaxWidth defaults on private helpers by @hxreborn

- [`1df441d`](https://github.com/hxreborn/three-finger-swipe/commit/1df441d53cc0f6c6a029b38843e3e6bdfe40a336) *(util)* Tolerate arg count and class name drift by API level by @hxreborn

- [`b4946b9`](https://github.com/hxreborn/three-finger-swipe/commit/b4946b98e5f46cbcc69a926cc34610126e700a60) Rename App.service to mService by @hxreborn


### Dependencies

- [`58a97ee`](https://github.com/hxreborn/three-finger-swipe/commit/58a97eea37519e39a7ee1ccecb28f6c0eb7d78b6) *(deps)* Bump io.github.libxposed:api from 101.0.0 to 101.0.1 by @dependabot[bot] in [#19](https://github.com/hxreborn/three-finger-swipe/pull/19)

- [`4b7d658`](https://github.com/hxreborn/three-finger-swipe/commit/4b7d658effc478e59e85f8e69fd6d10ac649dd29) *(deps)* Bump androidx.compose:compose-bom from 2026.03.00 to 2026.03.01 by @dependabot[bot] in [#18](https://github.com/hxreborn/three-finger-swipe/pull/18)

- [`3ea0db2`](https://github.com/hxreborn/three-finger-swipe/commit/3ea0db2298491ac7f11995247058aa1946bb1166) *(deps)* Bump androidx.compose.material3:material3 from 1.5.0-alpha15 to 1.5.0-alpha16 by @dependabot[bot] in [#17](https://github.com/hxreborn/three-finger-swipe/pull/17)

- [`e2e4d6f`](https://github.com/hxreborn/three-finger-swipe/commit/e2e4d6f4f31304fc95bb5204ff34bb6620307c36) *(deps)* Bump softprops/action-gh-release from 2 to 3 by @dependabot[bot] in [#22](https://github.com/hxreborn/three-finger-swipe/pull/22)

- [`bc50807`](https://github.com/hxreborn/three-finger-swipe/commit/bc50807a12a8c4f62b90b3b0653f169f01e8dab7) *(deps)* Bump gradle-wrapper from 9.4.0 to 9.4.1 by @dependabot[bot] in [#16](https://github.com/hxreborn/three-finger-swipe/pull/16)


### Build

- [`c130fb4`](https://github.com/hxreborn/three-finger-swipe/commit/c130fb40459a03091a3b0bf8952f6eb110acb039) *(cliff)* Sync to shared dotfiles template by @hxreborn

- [`62b6350`](https://github.com/hxreborn/three-finger-swipe/commit/62b6350f77d6f6c66a1405841745b3b34c89e706) Generate xposed module metadata and bump libxposed to 102.0.0 by @hxreborn


### Miscellaneous

- [`c1df58d`](https://github.com/hxreborn/three-finger-swipe/commit/c1df58d238ece12109fafae56ac6e85718635fe7) *(ci)* Skip docs style and release commits in the changelog config by @hxreborn

- [`19f0212`](https://github.com/hxreborn/three-finger-swipe/commit/19f0212bbb36de9f38767106036c54b2613e5006) *(xposed)* Set exceptionMode=protective by @hxreborn

- [`d09630d`](https://github.com/hxreborn/three-finger-swipe/commit/d09630de3fa37410d5ea23b0ba730a8436e3cd3f) Tighten the gitignore markdown allowlist and ignore docs by @hxreborn



## What's Changed



### Refactor

- [`90fdc3b`](https://github.com/hxreborn/three-finger-swipe/commit/90fdc3b2d604d066f54f7e7748897a84c0aa83fc) Use module name as log tag by @hxreborn


### Build

- [`833bbef`](https://github.com/hxreborn/three-finger-swipe/commit/833bbeff8f737e10141697c6499790d5d0fa2c18) *(gradle)* Remove libxposed git submodules by @hxreborn

- [`e9a3e2c`](https://github.com/hxreborn/three-finger-swipe/commit/e9a3e2cdfc697277f5c173298a5a623d7edc8df6) *(gradle)* Remove mavenLocal repository by @hxreborn

- [`65e40b3`](https://github.com/hxreborn/three-finger-swipe/commit/65e40b3d2bd61b48ae6b296bd387c197aa801fd8) *(gradle)* Remove libxposed submodule build tasks by @hxreborn



## What's Changed



### Bug Fixes

- [`0eb7efd`](https://github.com/hxreborn/three-finger-swipe/commit/0eb7efd2ff721bba11cba56d9ae8a512834ee00b) *(ci)* Update cliff.toml for git-cliff 2.x template API by @hxreborn

- [`7815e79`](https://github.com/hxreborn/three-finger-swipe/commit/7815e79d81f014ec3ead5c38fb605558156556ad) *(ui)* Fix licenses screen crash from AGP 9 resource obfuscation by @hxreborn



## What's Changed



### Features

- [`7752507`](https://github.com/hxreborn/three-finger-swipe/commit/7752507d366bcef5e7e63a6fa723af56e6a63113) Migrate to libxposed API 101 [**breaking**] by @hxreborn in [#14](https://github.com/hxreborn/three-finger-swipe/pull/14)


### Dependencies

- [`cb8bc48`](https://github.com/hxreborn/three-finger-swipe/commit/cb8bc4822d49118f69e4c18355a0762cc7b0e9cd) *(deps)* Bump me.zhanghai.compose.preference:preference by @dependabot[bot] in [#13](https://github.com/hxreborn/three-finger-swipe/pull/13)

- [`3fd0f74`](https://github.com/hxreborn/three-finger-swipe/commit/3fd0f743a6eb2b767e088a1a8508d09b3b65547a) *(deps)* Bump androidx.activity:activity-compose by @dependabot[bot] in [#12](https://github.com/hxreborn/three-finger-swipe/pull/12)

- [`28c25c7`](https://github.com/hxreborn/three-finger-swipe/commit/28c25c7700d619e7f8a25bba79cd6209c83a93e0) *(deps)* Bump kotlin from 2.3.10 to 2.3.20 by @dependabot[bot] in [#11](https://github.com/hxreborn/three-finger-swipe/pull/11)

- [`f2e0f7f`](https://github.com/hxreborn/three-finger-swipe/commit/f2e0f7fe4cd0fa9665545443b96d341737c41587) *(deps)* Bump org.jetbrains.kotlinx:kotlinx-serialization-core by @dependabot[bot] in [#8](https://github.com/hxreborn/three-finger-swipe/pull/8)


### Build

- [`6377568`](https://github.com/hxreborn/three-finger-swipe/commit/63775682103f5919a8a69b45f9498659075d1ee6) *(gradle)* Patch libxposed/service namespace at build time for AGP 9 by @hxreborn

- [`bc4a9eb`](https://github.com/hxreborn/three-finger-swipe/commit/bc4a9eb86551cc4e78c2e7fc39311e228f21c02c) *(gradle)* Upgrade AGP to 9.1.0, Gradle to 9.4.0 and bump dependencies by @hxreborn



## What's Changed



### Features

- [`f412333`](https://github.com/hxreborn/three-finger-swipe/commit/f412333970ebb1e2c2c2df84b819e965ac15e32c) *(action)* Implement screen off, flashlight, and ringer mode actions by @hxreborn

- [`87b698d`](https://github.com/hxreborn/three-finger-swipe/commit/87b698d648991bcb34cb58c0aeb6dfc7c11b8b87) *(action)* Initial scaffold for more features by @hxreborn

- [`b259b04`](https://github.com/hxreborn/three-finger-swipe/commit/b259b049a768044655a8b77698e9d92ee534debd) *(gesture)* Add initial crop screenshot capture and delivery by @hxreborn

- [`6a801f8`](https://github.com/hxreborn/three-finger-swipe/commit/6a801f8ce6c87b2504a7963ffb08a9d17b57fd25) *(gesture)* Add screenshot trigger and dispatch resolver by @hxreborn

- [`1b447c2`](https://github.com/hxreborn/three-finger-swipe/commit/1b447c212e70470aa3ab19cab905d2b79eed3cd8) *(gesture)* Add three-finger swipe state machine by @hxreborn

- [`2531880`](https://github.com/hxreborn/three-finger-swipe/commit/25318802a3ace5b2d05ad26d06276fe245e07c24) *(module)* Add Xposed entry point and system_server hook by @hxreborn

- [`180094a`](https://github.com/hxreborn/three-finger-swipe/commit/180094a624965217b933fd5e8850d3512212bf26) *(prefs)* Add step snapping and state restore support by @hxreborn

- [`d0503a1`](https://github.com/hxreborn/three-finger-swipe/commit/d0503a1d0a172ebca28f0dd78b24561629deb1ca) *(prefs)* Add gesture tuning and capture mode pref specs by @hxreborn

- [`07e12d7`](https://github.com/hxreborn/three-finger-swipe/commit/07e12d7bff13e9b7e63036149380c9cee55f35ed) *(prefs)* Add CaptureMode enum by @hxreborn

- [`73cf346`](https://github.com/hxreborn/three-finger-swipe/commit/73cf346c207429aedf817f043b53cf142c4ad2ff) *(prefs)* Add preference system with remote bridge by @hxreborn

- [`5776910`](https://github.com/hxreborn/three-finger-swipe/commit/577691075d28db1cb34e421c908ed1bfcbc4cf40) *(ui)* Add About screen with Material You header and Licenses screen by @hxreborn

- [`92db0c1`](https://github.com/hxreborn/three-finger-swipe/commit/92db0c1596905bde5dc9b5e470c19171a6ea6abd) *(ui)* Replace single settings screen with multi-screen navigation by @hxreborn

- [`4cd2f49`](https://github.com/hxreborn/three-finger-swipe/commit/4cd2f49d66937c3fe96e8bee2909c0a866024e8f) *(ui)* Add gesture tuning and capture mode settings by @hxreborn

- [`c08b1d4`](https://github.com/hxreborn/three-finger-swipe/commit/c08b1d4922c48874ca7087d6ebadb1ce8c96f6b9) *(ui)* Add app icons and splash animation by @hxreborn

- [`17ce971`](https://github.com/hxreborn/three-finger-swipe/commit/17ce971801ba86282365c04d21016f5b0a389e97) *(ui)* Add settings screen and companion app by @hxreborn

- [`ff8b59d`](https://github.com/hxreborn/three-finger-swipe/commit/ff8b59d478be84d4c35f7b5330ac3631acc1ac3f) *(ui)* Add Material 3 Expressive theme by @hxreborn


### Bug Fixes

- [`10a2978`](https://github.com/hxreborn/three-finger-swipe/commit/10a29782d9429b4b45634b638b6f10c8f7a80585) *(build)* Drop custom collect task, use plugin-registered exportLibraryDefinitions by @hxreborn

- [`e2d53ac`](https://github.com/hxreborn/three-finger-swipe/commit/e2d53ac751dc216028db63bfa292b641ab4e27cb) *(build)* Simplify aboutLibraries release variant task by @hxreborn

- [`291fa6e`](https://github.com/hxreborn/three-finger-swipe/commit/291fa6e387843b045cebf389ab82fe62e4f80f87) *(ui)* Make action picker dialog scrollable by @hxreborn

- [`773f82a`](https://github.com/hxreborn/three-finger-swipe/commit/773f82a88e0ad4e3a8e116ab56aaf64ea1adf711) *(ui)* Replace swipe toggle with inline action picker dialog by @hxreborn

- [`4bec61c`](https://github.com/hxreborn/three-finger-swipe/commit/4bec61ca9314527dc5e9341bd70b9f068a159306) *(ui)* Derive module status from XposedService binding by @hxreborn


### Refactor

- [`b565987`](https://github.com/hxreborn/three-finger-swipe/commit/b56598749e8642a72c418230c2a223c8068d773f) *(action)* Remove stub actions and unused action IDs by @hxreborn

- [`2743a74`](https://github.com/hxreborn/three-finger-swipe/commit/2743a740a257de3ecd07cf25d9f1612a9caef189) *(gesture)* Rename ThreeFingerSwipeHandler to GestureHandler by @hxreborn

- [`ab29d9c`](https://github.com/hxreborn/three-finger-swipe/commit/ab29d9ce56feacef3ef84ae00bfdc3fbb497e0e5) *(hooks)* Trim screenshot dispatch helpers by @hxreborn

- [`5c2778f`](https://github.com/hxreborn/three-finger-swipe/commit/5c2778fd465b749cac446b65d77afd1a0c59ae62) *(hooks)* Enrich SYSRQ dispatch logs by @hxreborn

- [`a7b2e30`](https://github.com/hxreborn/three-finger-swipe/commit/a7b2e3008ffd68789892633d9b1e6a5118fceef9) *(hooks)* Integrate CaptureMode and GestureConfig into hook chain by @hxreborn

- [`f56f591`](https://github.com/hxreborn/three-finger-swipe/commit/f56f591d2de8fd5fa337965c831506826f84f83d) *(hooks)* Cache swipe threshold and use readOrDefault by @hxreborn

- [`6c95387`](https://github.com/hxreborn/three-finger-swipe/commit/6c953877b8c4090bf38267151acc4e57d4944c07) *(hooks)* Simplify screenshot dispatch with multi-path resolution by @hxreborn

- [`760ceab`](https://github.com/hxreborn/three-finger-swipe/commit/760ceabed4c201bb5d81438b8d4c82f628606128) *(hooks)* Add findAllMethodsUpward and Method.signature helpers by @hxreborn

- [`5cab9e7`](https://github.com/hxreborn/three-finger-swipe/commit/5cab9e7deb051e4612e07ec067fbf9558e9a8c17) *(hooks)* Drop crop gateway binding by @hxreborn

- [`ba7f959`](https://github.com/hxreborn/three-finger-swipe/commit/ba7f9592aab3666a24e3dccf0f1143fa52bccf29) *(module)* Clean up gesture handler by @hxreborn

- [`82f1d3e`](https://github.com/hxreborn/three-finger-swipe/commit/82f1d3e03cac7fe821ddc085234bcd3bfcf5ea57) *(module)* Simplify screenshot trigger by @hxreborn

- [`538cda6`](https://github.com/hxreborn/three-finger-swipe/commit/538cda6ac2e8262530c2e4405d7044eeb2c2a406) *(module)* Simplify dispatch resolver by @hxreborn

- [`38053db`](https://github.com/hxreborn/three-finger-swipe/commit/38053db5661e4db5ca43541acfe2d95cc3499037) *(module)* Remove display capture gateway by @hxreborn

- [`f486ef3`](https://github.com/hxreborn/three-finger-swipe/commit/f486ef30296b82cae9e65d9612bb5341e2d9a4ef) *(module)* Remove screenshot delivery by @hxreborn

- [`e388a2c`](https://github.com/hxreborn/three-finger-swipe/commit/e388a2cb50294c93ea4400f8e98748d16480f0f0) *(module)* Remove crop capture core by @hxreborn

- [`c1e4da2`](https://github.com/hxreborn/three-finger-swipe/commit/c1e4da2dffc68ce206384af41fba137c33df4f33) *(prefs)* Source GestureConfig defaults from Prefs by @hxreborn

- [`1e00bb2`](https://github.com/hxreborn/three-finger-swipe/commit/1e00bb218521a1d5e6df0ae438eccb0a5419de21) *(prefs)* Rename CaptureMode.REFLECTION to SYSTEM_API by @hxreborn

- [`5768e45`](https://github.com/hxreborn/three-finger-swipe/commit/5768e459adb687b37315a1c79c6172959f2c1581) *(prefs)* Remove SWIPE_ENABLED, use NO_ACTION to disable gesture by @hxreborn

- [`1758972`](https://github.com/hxreborn/three-finger-swipe/commit/17589729b67bc5169fe6bc60dead37daa13fa4a8) *(prefs)* Expand PrefsState and extract pushToRemote by @hxreborn

- [`ca61829`](https://github.com/hxreborn/three-finger-swipe/commit/ca6182906cdd8443c1b08760ea8c0f0a9ee550a5) *(prefs)* Add IntPref range validation and readOrDefault extension by @hxreborn

- [`34856b0`](https://github.com/hxreborn/three-finger-swipe/commit/34856b042df426b91139cb125a1e6ead43d55cec) *(prefs)* Remove crop prefs by @hxreborn

- [`222120b`](https://github.com/hxreborn/three-finger-swipe/commit/222120bc3d9291d8b32a6db9504aa36fb3cf5e54) *(strings)* Align generic action naming by @hxreborn

- [`6f809a6`](https://github.com/hxreborn/three-finger-swipe/commit/6f809a6d5c80175264af102099910850064fa9e6) *(ui)* Replace per-pref setters with generic savePref by @hxreborn

- [`b3dc35f`](https://github.com/hxreborn/three-finger-swipe/commit/b3dc35f5a6cac799de568e1ae2ddaa91997ecd36) *(ui)* Simplify gesture illustration by @hxreborn

- [`3d162c2`](https://github.com/hxreborn/three-finger-swipe/commit/3d162c29a3881d4e741632afdd7e5fea17d5dcf0) *(ui)* Remove crop settings by @hxreborn

- [`d456e79`](https://github.com/hxreborn/three-finger-swipe/commit/d456e79d01b7e9d1c76874248044d8acbf72be07) Replace null checks with idiomatic Kotlin scope functions by @hxreborn


### Build

- [`d7047c0`](https://github.com/hxreborn/three-finger-swipe/commit/d7047c06e304f949efa18c8773dd716ec9c94eef) *(gradle)* Add aboutlibraries plugin and GIT_HASH buildConfigField by @hxreborn

- [`1937eb1`](https://github.com/hxreborn/three-finger-swipe/commit/1937eb123853de0278456ccd673a1b2293252500) *(gradle)* Add navigation3 and kotlinx-serialization dependencies by @hxreborn

- [`9b44859`](https://github.com/hxreborn/three-finger-swipe/commit/9b44859cb6b3e491fc01b65a0fe1a2f7984e5b64) *(gradle)* Ignore dirty state in libxposed submodules by @hxreborn

- [`9d31183`](https://github.com/hxreborn/three-finger-swipe/commit/9d31183f67976a7279e05ce55b204b668358ef2e) *(gradle)* Add libxposed submodules pinned to API 100 by @hxreborn

- [`7487f70`](https://github.com/hxreborn/three-finger-swipe/commit/7487f709ab3ae6f71dc664266dfb97b9a69db0f3) *(gradle)* Add project scaffolding by @hxreborn

- [`1189ebc`](https://github.com/hxreborn/three-finger-swipe/commit/1189ebc34d11e9c74351aa9c93b549db03f50033) *(release)* Fix version code formula and drop manual override by @hxreborn


### Miscellaneous

- [`7067346`](https://github.com/hxreborn/three-finger-swipe/commit/7067346568b1a579929d75c588ce645f3683c17e) *(ci)* Sign release tags and prompt on missing changelog by @hxreborn

- [`6d10f1e`](https://github.com/hxreborn/three-finger-swipe/commit/6d10f1ee64f17116a4c4e119990e9e226bb3b51f) *(ci)* Disable mirror push in release by @hxreborn

- [`1c21084`](https://github.com/hxreborn/three-finger-swipe/commit/1c210848ff7afb50daf8fcfdb5bb0deefeeb872f) *(ci)* Use deterministic version code by @hxreborn

- [`a46b99e`](https://github.com/hxreborn/three-finger-swipe/commit/a46b99eb56df11555639ff82aaa2728a35b22bf4) *(hooks)* Remove dead ModuleSettingsStore ProGuard rule by @hxreborn

- [`d38938e`](https://github.com/hxreborn/three-finger-swipe/commit/d38938ec93ec0890e4d2168c9d1a5210349d4918) *(view)* Update swipe down lottie asset by @hxreborn

- [`a3dc08a`](https://github.com/hxreborn/three-finger-swipe/commit/a3dc08a36f66c84c701d62f4323fb99c538ea51b) *(view)* Shrink swipe lottie asset by @hxreborn

- [`a9a39d9`](https://github.com/hxreborn/three-finger-swipe/commit/a9a39d96db02d9444d7c9f90d7de0b3d3805789e) *(view)* Clean up strings and icon by @hxreborn

- [`29e44c8`](https://github.com/hxreborn/three-finger-swipe/commit/29e44c8e101308f05f759b3bd84a77477a4d47d0) Remove stale fastlane changelog 1000000 by @hxreborn

- [`51161a5`](https://github.com/hxreborn/three-finger-swipe/commit/51161a50f77b5733cbc369b8b892dbd702f52b90) Rename fastlane changelog to match new version code formula by @hxreborn

- [`2044669`](https://github.com/hxreborn/three-finger-swipe/commit/2044669a9b6bbae14ccb5bea0ae04e0e250568ba) Release v1.0.0-beta1 by @hxreborn

- [`8eab101`](https://github.com/hxreborn/three-finger-swipe/commit/8eab101cdb88b7e2bd0425f3fd242dbe9592a687) Add Android resources by @hxreborn

- [`c1ae3de`](https://github.com/hxreborn/three-finger-swipe/commit/c1ae3de4b92e6f3302762dd034c27cb64c8b72af) Add .gitignore and .editorconfig by @hxreborn




