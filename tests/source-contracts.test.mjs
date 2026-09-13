// Author/creator: nattapat2871 (https://nattapat2871.me)

import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const read = (relativePath) => readFile(new URL('../' + relativePath, import.meta.url), 'utf8')

test('keeps the private icon font scoped to the icon instead of the player name', async () => {
  const componentSources = await Promise.all([
    'game-bridge/src/client/java/com/namlauncher/bridge/badge/BadgeComponents.java',
    'game-companions/fabric-1.20.1/src/main/java/com/namlauncher/bridge/fabric1201/BadgeComponents.java',
    'game-companions/fabric-1.21.1/src/main/java/com/namlauncher/bridge/fabric1201/BadgeComponents.java',
    'game-companions/fabric-1.21.11/src/main/java/com/namlauncher/bridge/badge/BadgeComponents.java',
    'game-companions/forge-1.20.1/src/main/java/com/namlauncher/bridge/forge1201/BadgeComponents.java'
  ].map(read))

  for (const source of componentSources) {
    assert.match(source, /return Component\.empty\(\)[\s\S]*\.append\(Component\.literal\(GLYPH\)\.withStyle\(/)
    assert.doesNotMatch(source, /return Component\.literal\(GLYPH\)\.withStyle\(/)
  }
})

test('keeps the canonical Fabric bridge compatible with newer loader releases', async () => {
  const metadata = await read('game-bridge/src/main/resources/fabric.mod.json')
  assert.match(metadata, /"fabricloader":\s*">=\$\{loader_version\}"/)
})

test('exports from the private repository into an explicit launcher destination', async () => {
  const [buildSource, exporterSource] = await Promise.all([
    read('game-bridge/build.gradle'),
    read('scripts/export-launcher-bundle.mjs')
  ])
  assert.match(buildSource, /tasks\.register\('canonicalizeBridgeJar'\)/)
  assert.match(exporterSource, /process\.argv\.indexOf\('--destination'\)/)
  assert.match(exporterSource, /'prebuilt', 'legacy'/)
  assert.match(exporterSource, /'assets', 'NamLauncher-icon\.png'/)
  assert.match(exporterSource, /'dist', 'launcher-bundle'/)
})

test('all companion builds source the canonical icon from this repository', async () => {
  const buildFiles = [
    'game-bridge/build.gradle',
    'game-companions/fabric-1.20.1/build.gradle',
    'game-companions/fabric-1.21.1/build.gradle',
    'game-companions/fabric-1.21.11/build.gradle',
    'game-companions/forge-1.20.1/build.gradle',
    'game-companions/forge-1.21.11/build.gradle',
    'game-companions/forge-26.2/build.gradle',
    'game-companions/neoforge-1.21.1/build.gradle',
    'game-companions/neoforge-1.21.11/build.gradle',
    'game-companions/neoforge-26.2/build.gradle'
  ]
  const buildSources = await Promise.all(buildFiles.map(read))
  for (const source of buildSources) {
    assert.match(source, /assets\/NamLauncher-icon\.png/)
    assert.doesNotMatch(source, /from\(['"](?:\.\.\/){1,2}NamLauncher-icon\.png['"]\)/)
  }
})
