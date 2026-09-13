# Private Companion Security Policy

- Keep this repository private.
- Never commit launcher tokens, server credentials, signing private keys,
  player data, production logs, databases, or `.qa` artifacts.
- Treat every prebuilt JAR as immutable: verify its pinned SHA-256 and size.
- Build maintained targets only from reviewed source and supported toolchains.
- Export bundles only through `scripts/export-launcher-bundle.mjs`.
- Run the complete Node and Gradle test matrix plus a secret scan before push.
- Report suspected exposure privately to the repository owner; do not open a
  public issue containing sensitive data.

Author/creator: [nattapat2871](https://nattapat2871.me)
