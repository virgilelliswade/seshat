# Seshat Workspace Rules

## 1. Mandatory Semantic Versioning on Every Update
Whenever you build, package, or ship an update to Seshat, you **MUST** increment the version number across the project based on the significance of the change before building and pushing:
- **Patch (`X.Y.Z` → `X.Y.(Z+1)`)**: Bug fixes, UI polish, layout adjustments, copy tweaks, or incremental refinements (e.g., `2.0.0` → `2.0.1`).
- **Minor (`X.Y.Z` → `X.(Y+1).0`)**: New features, new game modes, new tools, or substantial workflow additions (e.g., `2.0.1` → `2.1.0`).
- **Major (`X.Y.Z` → `(X+1).0.0`)**: Architectural overhauls, major paradigm shifts, or breaking changes.
- Always increment `versionCode` by `+1` in `app/build.gradle.kts` alongside every version change.

### Version Locations to Update Together
Whenever bumping the version to `<VERSION>`, update all of the following in lockstep:
1. `app/build.gradle.kts`: `versionCode` (+1) and `versionName = "<VERSION>"`
2. `composeApp/build.gradle.kts`: `packageVersion = "<VERSION>"`
3. `composeApp/src/commonMain/kotlin/com/seshat/writer/ui/AboutDialog.kt` & `app/src/main/java/com/seshat/writer/ui/AboutDialog.kt`: `"Version <VERSION> • Studio & First Draft Engine"`
4. `composeApp/src/commonMain/kotlin/com/seshat/writer/bundle/SeshatBundleManager.kt` & `app/src/main/java/com/seshat/writer/bundle/SeshatBundleManager.kt`: `put("seshatVersion", "<VERSION>")`
5. `composeApp/src/commonMain/kotlin/com/seshat/writer/firstdraft/ReceiptGenerator.kt` & `app/src/main/java/com/seshat/writer/firstdraft/ReceiptGenerator.kt`: `"SESHAT v<VERSION> • ANTI-PROCRASTINATION ENGINE"`
6. `composeApp/src/commonMain/kotlin/com/seshat/writer/firstdraft/FirstDraftController.kt` & `app/src/main/java/com/seshat/writer/firstdraft/FirstDraftController.kt`: `"🏆 FADE OUT AT 17,000+ WORDS! STUDIO UNLOCKED — Full Seshat v<VERSION> Unlocked!"`

## 2. Multiplatform Parity (`composeApp` & `app`)
- Keep macOS Desktop (`composeApp/src/commonMain/kotlin/com/seshat/writer/...`) and Android/ChromeOS (`app/src/main/java/com/seshat/writer/...`) 100% synchronized whenever modifying shared UI, models, or controllers.

## 3. Release Deliverables & Git Tagging
- When packaging a release for `<VERSION>`, build both `:composeApp:packageDmg` and `:app:assembleDebug`.
- Copy the resulting `Seshat-<VERSION>.dmg` and `Seshat-v<VERSION>.apk` to both the project root (`/Users/virgilelliswade/Downloads/Writing App/`) and `/Users/virgilelliswade/Downloads/`, removing outdated versioned binaries from the project root.
- Commit and push changes to `origin/main` and create/push the corresponding git tag `v<VERSION>`.
