# NamLauncher Minecraft Companions

Private source repository for the Minecraft-side components bundled with
NamLauncher. It owns the Fabric, Forge, and NeoForge implementations, their
compatibility tests, the protected badge assets, and the reproducible launcher
bundle exporter.

This repository must remain private. The public launcher repository contains
only the verified JAR bundle and its SHA-256 manifest; it does not contain these
Java sources or build tooling.

## Supported maintained matrix

| Minecraft | Fabric | Forge | NeoForge |
| --- | --- | --- | --- |
| 1.21.11 | yes | yes | yes |
| 26.2 | yes | yes | yes |

The frozen 1.20.1 and 1.21.1 compatibility JARs live under
`prebuilt/legacy/`. Their exact size and SHA-256 values are pinned by the
exporter and are never rebuilt under an existing version.

## Verify and export

Install the Node dependency and run the source contracts:

```powershell
npm ci
npm test
```

Build all maintained targets and create `dist/launcher-bundle`:

```powershell
npm run build:bundle
```

Export directly into a separate NamLauncher checkout:

```powershell
node scripts/export-launcher-bundle.mjs --destination ..\NamLauncher\build\game-bridge
```

The exporter validates the canonical icon, mod identity, safe Mixin policy,
artifact sizes, exact hashes, and final manifest inventory before completing.
Configure `NAMLAUNCHER_JAVA17_HOME`, `NAMLAUNCHER_JAVA21_HOME`,
`NAMLAUNCHER_JAVA25_HOME`, and `NAMLAUNCHER_GRADLE8_HOME` when the required
toolchains are not in their documented local locations.

## Repository boundary

- This repo owns Minecraft mod source and the bundle exporter.
- `NamLauncher/NamLauncher` owns the Electron launcher and a verified bundle.
- `NamLauncher/platform` owns the API, website, Discord integration, and data
  schemas.
- `NamLauncher/operations` owns deployment templates and runbooks.
- Never commit tokens, signing keys, `.env` files, player data, production
  databases, logs, or `.qa` workspaces.

Author/creator: [nattapat2871](https://nattapat2871.me)
