# Security Policy

## Supported versions

Security fixes are applied to the maintained Minecraft matrix listed in the
README. Frozen compatibility builds receive critical fixes when technically
possible.

## Reporting a vulnerability

Please use GitHub's private vulnerability reporting option in the Security tab.
Do not open a public issue containing exploit details, credentials, player data,
or production information. Include affected Minecraft and loader versions,
reproduction steps, impact, and any suggested mitigation.

## Maintainer release rules

- Never commit launcher tokens, server credentials, signing private keys,
  player data, production logs, databases, or `.qa` artifacts.
- Treat every prebuilt JAR as immutable: verify its pinned SHA-256 and size.
- Build maintained targets only from reviewed source and supported toolchains.
- Export bundles only through `scripts/export-launcher-bundle.mjs`.
- Run the complete Node and Gradle test matrix plus a secret scan before push.

Author/creator: [nattapat2871](https://nattapat2871.me)
