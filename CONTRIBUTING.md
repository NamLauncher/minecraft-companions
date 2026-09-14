# Contributing to NamLauncher Minecraft Companions

Thank you for helping improve NamLauncher. Keep changes focused on the client
companion, include the affected Minecraft and loader versions, and preserve the
client-only boundary.

## Before opening a pull request

1. Create a focused branch from `main`.
2. Do not add credentials, production data, logs, signing material, or private
   service configuration.
3. Run `npm ci` and `npm test`.
4. Run `npm run build:bundle` when changing Java source, resources, metadata,
   build files, or the exporter.
5. Explain user-visible behavior and list the targets you tested.

The CI workflow is the authoritative cross-target build check. A successful
static build is not a substitute for an in-game test; state clearly which level
of verification was performed.

By contributing, you agree that your contribution is licensed under the
GPL-3.0-only license used by this repository.

Author/creator: [nattapat2871](https://nattapat2871.me)
