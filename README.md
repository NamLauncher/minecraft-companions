# NamLauncher Minecraft Companions

Open-source Minecraft client companions bundled and managed by NamLauncher.
This repository contains the Fabric, Forge, and NeoForge implementations,
compatibility tests, badge assets, and the reproducible launcher bundle
exporter.

The companion adds NamLauncher branding and an optional verified-player badge
to supported Minecraft clients. It is client-only: no server installation is
required, and it does not grant gameplay permissions or bypass server rules.

## Features

- Displays the NamLauncher badge before a verified player's name in chat, the
  player list, and supported name tags, with consistent spacing.
- Provides `/namlauncher` client preferences for supported badge surfaces.
- Keeps badge assets isolated from normal resource-pack overrides.
- Adds bounded NamLauncher branding to the window title and supported F3
  entries.
- Supports launcher-managed integrity verification and restoration before the
  game starts.

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

- This public repo owns the Minecraft companion source and bundle exporter.
- `NamLauncher/NamLauncher` owns the Electron launcher and a verified bundle.
- `NamLauncher/platform` owns the API, website, Discord integration, and data
  schemas.
- `NamLauncher/operations` owns deployment templates and runbooks.
- Never commit tokens, signing keys, `.env` files, player data, production
  databases, logs, or `.qa` workspaces.

## Contributing and security

Issues and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md)
before submitting a change. Report security vulnerabilities using the private
process described in [SECURITY.md](SECURITY.md), never in a public issue.

## License

Copyright (c) 2026 nattapat2871. This project is licensed under
[GPL-3.0-only](LICENSE). Third-party components retain their own licenses.

Author/creator: [nattapat2871](https://nattapat2871.me)
