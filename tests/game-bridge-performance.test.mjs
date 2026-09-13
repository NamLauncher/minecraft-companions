// Author/creator: nattapat2871 (https://nattapat2871.me)
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import test from 'node:test'

const tagSource = await readFile(new URL(
  '../game-bridge/src/client/java/com/namlauncher/bridge/badge/ServerNameTagBadge.java',
  import.meta.url
), 'utf8')
const textDisplayMixinSource = await readFile(new URL(
  '../game-bridge/src/client/java/com/namlauncher/bridge/mixin/TextDisplayBadgeMixin.java',
  import.meta.url
), 'utf8')
const badgeLookupSource = await readFile(new URL(
  '../game-bridge/src/client/java/com/namlauncher/bridge/badge/PlayerBadgeLookupService.java',
  import.meta.url
), 'utf8')
const modernBadgeLookupSources = await Promise.all([
  '../game-bridge/src/client/java/com/namlauncher/bridge/badge/PlayerBadgeLookupService.java',
  '../game-companions/neoforge-1.21.11/src/main/java/com/namlauncher/bridge/badge/PlayerBadgeLookupService.java',
  '../game-companions/neoforge-26.2/src/main/java/com/namlauncher/bridge/badge/PlayerBadgeLookupService.java'
].map((path) => readFile(new URL(path, import.meta.url), 'utf8')))
const badgeComponentsSource = await readFile(new URL(
  '../game-bridge/src/client/java/com/namlauncher/bridge/badge/BadgeComponents.java',
  import.meta.url
), 'utf8')
const modernBadgeComponentSources = await Promise.all([
  '../game-bridge/src/client/java/com/namlauncher/bridge/badge/BadgeComponents.java',
  '../game-companions/fabric-1.21.11/src/main/java/com/namlauncher/bridge/badge/BadgeComponents.java'
].map((path) => readFile(new URL(path, import.meta.url), 'utf8')))
const badgeRegistrySource = await readFile(new URL(
  '../game-bridge/src/main/java/com/namlauncher/bridge/badge/BadgeRegistry.java',
  import.meta.url
), 'utf8')
const badgeAliasesSource = await readFile(new URL(
  '../game-bridge/src/main/java/com/namlauncher/bridge/badge/BadgeIdentityAliases.java',
  import.meta.url
), 'utf8')
const mixinConfigs = await Promise.all([
  '../game-bridge/src/client/resources/namlauncher-branding-bridge.client.mixins.json',
  '../game-companions/forge-26.2/src/main/resources/namlauncher-game-companion.forge-26.2.mixins.json',
  '../game-companions/neoforge-26.2/src/main/resources/namlauncher-game-companion.neoforge-26.2.mixins.json'
].map(async (path) => JSON.parse(await readFile(new URL(path, import.meta.url), 'utf8'))))
const legacyMixinConfigs = await Promise.all([
  '../game-companions/fabric-1.21.11/src/main/resources/namlauncher-game-companion.fabric-1.21.11.mixins.json',
  '../game-companions/forge-1.21.11/src/main/resources/namlauncher-game-companion.forge-1.21.11.mixins.json',
  '../game-companions/neoforge-1.21.11/src/main/resources/namlauncher-game-companion.neoforge-1.21.11.mixins.json'
].map(async (path) => JSON.parse(await readFile(new URL(path, import.meta.url), 'utf8'))))

test('server label badges avoid scanning every online player on every frame', () => {
  assert.doesNotMatch(tagSource, /minecraft\.level\.players\(\)/)
  assert.match(tagSource, /BadgeRegistry\.snapshot\(\)/)
  assert.match(tagSource, /getPlayerByUUID\(playerUuid\)/)
  assert.match(tagSource, /CACHE_TICKS/)
  assert.match(tagSource, /WeakHashMap/)
  assert.match(tagSource, /CACHE_TICKS = 40/)
})

test('badge discovery refreshes on server join and calls the API only once per minute afterward', () => {
  assert.match(badgeLookupSource, /LOOKUP_INTERVAL_SECONDS = 60/)
  assert.match(badgeAliasesSource, /MAX_VISIBLE_PLAYERS = 512/)
  for (const source of modernBadgeLookupSources) {
    assert.match(source, /CONNECTION_POLL_SECONDS = 1/)
    assert.match(source, /LOOKUP_INTERVAL_SECONDS = 60/)
    assert.match(source, /nextLookupAtNanos = 0L/)
    assert.match(source, /now < nextLookupAtNanos \|\| !inFlight\.compareAndSet\(false, true\)/)
    assert.match(source, /nextLookupAtNanos = now \+ LOOKUP_INTERVAL_NANOS/)
    assert.doesNotMatch(source, /JOIN_RECONCILE/)
    assert.doesNotMatch(source, /scheduleWithFixedDelay\([^\n]*, 2, 15,/)
  }
  assert.match(badgeRegistrySource, /current\.equals\(next\) \? current : next/)
})

test('tab and chat matching reuse the cached player-name set without rebuilding hash sets', () => {
  assert.match(badgeComponentsSource, /playerNames\.contains\(playerName\)/)
  for (const source of modernBadgeComponentSources) {
    assert.doesNotMatch(source, /new HashSet/)
    assert.match(source, /playerNames\.contains\(playerName\)/)
    assert.match(source, /boolean isDecorated/)
  }
})

test('decorated text display line layout is reused instead of rebuilt every frame', () => {
  assert.match(textDisplayMixinSource, /namlauncher\$lineCache/)
  assert.match(textDisplayMixinSource, /TextDisplayBadgeRenderState\.faceCamera/)
  assert.equal((textDisplayMixinSource.match(/splitLines\(decorated, lineWidth\)/g) || []).length, 1)
})

test('final chat-row badge hooks are enabled on every maintained loader', () => {
  for (const config of mixinConfigs) {
    assert.ok(config.client.includes('ChatComponentBadgeMixin'))
  }
  for (const config of legacyMixinConfigs) {
    assert.ok(config.client.includes('ChatComponent12111BadgeMixin'))
  }
})
