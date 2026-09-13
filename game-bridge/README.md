# NamLauncher Branding Bridge

Author/creator: nattapat2871 (https://nattapat2871.me)

This is the canonical launcher-managed, client-only Fabric companion for
**Minecraft 26.2**. Together with the projects under `game-companions/`, the
v1.1.15 compatibility matrix covers Fabric, Forge, and NeoForge at the 1.20.1,
1.21.x, and 26.2 anchors. Supported companions brand the game window and F3
version line and can show the NamLauncher icon in nametags and the Tab List
while preserving loader and diagnostic context.

## Safety boundary

- Builds use Fabric Loader `0.19.3`; runtime metadata accepts compatible newer
  loaders. Minecraft remains pinned to `26.2` in metadata and the runtime gate.
- The bridge never patches, replaces, or redistributes a Minecraft JAR.
- Mixins are client-only, optional, and use `require = 0`. If a target changes,
  the relevant branding injection is skipped instead of crashing the game.
- The 26.2 targets are `Minecraft#createTitle()` and
  `DebugEntryVersion#display(...)`; automated tests verify both signatures
  against the resolved Minecraft dependency.
- Unknown title/header formats are returned unchanged, preserving compatibility
  with other client mods.
- A last-priority built-in client pack serves only `namlauncher:font/badge.json`
  and `namlauncher:textures/font/badge.png` from private bundled resources. It is
  recreated for resource reloads so server-pack namespace filters cannot hide
  the badge. No other namespace, server font, or datapack is replaced.
- This module does not modify authentication, multiplayer, LAN, networking, or
  server behavior.

## Reproducible build

The official Fabric 26.2 guidance specifies Loom 1.17, Gradle 9.5.1, Java 25,
and Fabric Loader 0.19.3. This module pins the resolved Loom release to
`1.17.19`. Build and test with:

```powershell
./gradlew.bat clean test build --no-daemon
```

The managed artifact is:

```text
game-bridge/build/libs/namlauncher-branding-bridge-1.1.15+26.2.jar
```

Archive tasks use stable entry ordering and omit file timestamps. The launcher
must still calculate and verify a release SHA-256 before installing the artifact.

To build, test, checksum, and stage the artifact for Electron packaging, run:

```powershell
node ../scripts/prepare-game-bridge.mjs --clean-after-stage
```

This keeps the verified JAR and manifest under `build/game-bridge/` while
removing the module's transient `build/` output and project-local `.gradle/`
cache. It never removes the shared user Gradle cache.

## Launcher integration contract

The launcher may install this artifact only when all conditions are true:

1. instance loader is Fabric;
2. instance Minecraft version equals `26.2` exactly;
3. selected Fabric Loader version equals `0.19.3` exactly;
4. the staged artifact SHA-256 matches the release manifest;
5. the destination is the selected instance's validated `mods` directory.

Use a launcher-owned deterministic filename and atomic replace. Do not delete or
overwrite a same-named file unless its Fabric metadata ID is
`namlauncher-branding-bridge`. Remove/disable the managed bridge when any gate no
longer matches.

## Verified upstream references

- Fabric 26.2 toolchain guidance: https://www.fabricmc.net/2026/06/15/262.html
- Fabric 26.2 example project: https://github.com/FabricMC/fabric-example-mod/tree/26.2
- Loom plugin selection for unobfuscated 26.2:
  https://docs.fabricmc.net/develop/loom/
- Fabric metadata/version constraints:
  https://docs.fabricmc.net/develop/loader/fabric-mod-json
