// Author/creator: nattapat2871 (https://nattapat2871.me)

import { createHash } from 'node:crypto'
import { existsSync } from 'node:fs'
import {
  copyFile,
  lstat,
  mkdir,
  readFile,
  readdir,
  realpath,
  rename,
  rm,
  stat,
  writeFile
} from 'node:fs/promises'
import path from 'node:path'
import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import AdmZip from 'adm-zip'

const SCRIPT_DIR = path.dirname(fileURLToPath(import.meta.url))
const WORKSPACE_ROOT = path.resolve(SCRIPT_DIR, '..')
const BRIDGE_ROOT = path.join(WORKSPACE_ROOT, 'game-bridge')
const WRAPPER_JAR = path.join(BRIDGE_ROOT, 'gradle', 'wrapper', 'gradle-wrapper.jar')
const destinationIndex = process.argv.indexOf('--destination')
if (destinationIndex >= 0 && (!process.argv[destinationIndex + 1] || process.argv[destinationIndex + 1].startsWith('--'))) {
  throw new Error('--destination requires a directory path')
}
const STAGE_DIR = path.resolve(
  destinationIndex >= 0 ? process.argv[destinationIndex + 1] : path.join(WORKSPACE_ROOT, 'dist', 'launcher-bundle')
)
const LEGACY_DIR = path.join(WORKSPACE_ROOT, 'prebuilt', 'legacy')
const AUTHOR = 'nattapat2871 (https://nattapat2871.me)'
const LAUNCHER_VERSION = '1.2.4'
const MAX_ARTIFACT_BYTES = 16 * 1024 * 1024
const MOD_ICON_ENTRY = 'assets/namlauncher/textures/font/badge.png'
const MOD_ICON_SOURCE = path.join(WORKSPACE_ROOT, 'assets', 'NamLauncher-icon.png')
const MANAGED_STAGE_FILE = /^(?:game-bridge-manifest\.json|namlauncher-(?:branding-bridge|game-companion)-[A-Za-z0-9.+-]+\.jar)$/

const retainedArtifacts = [
  {
    loader: 'fabric', minecraftVersion: '1.20.1', version: '1.1.16+1.20.1',
    filename: 'namlauncher-game-companion-fabric-1.20.1-1.1.16+1.20.1.jar',
    sha256: 'c2e2c2793061113f9a520f127d257b97b31b2150062f42e2088d98ee50e8c50c', size: 606946
  },
  {
    loader: 'forge', minecraftVersion: '1.20.1', version: '1.1.16+1.20.1',
    filename: 'namlauncher-game-companion-forge-1.20.1-1.1.16+1.20.1.jar',
    sha256: 'd02f6247ed5a1d1244fc17ae0cb35a4558f27f8ca69589f368fb307cd8bf9170', size: 607360
  },
  {
    loader: 'fabric', minecraftVersion: '1.21.1', version: '1.1.16+1.21.1',
    filename: 'namlauncher-game-companion-fabric-1.21.1-1.1.16+1.21.1.jar',
    sha256: '71bec105b1833b3d426c49c3709fa30c01b625b4e98a2c0581f6bd84da0d2130', size: 607098
  },
  {
    loader: 'neoforge', minecraftVersion: '1.21.1', version: '1.1.16+1.21.1',
    filename: 'namlauncher-game-companion-neoforge-1.21.1-1.1.16+1.21.1.jar',
    sha256: '4f61cb3f371c1f67df56e2fb91325b2dcee683e9b9aab7a915728371061baf2e', size: 605292
  }
].map((entry) => ({
  id: 'namlauncher-game-companion', launcherVersion: LAUNCHER_VERSION,
  environment: 'client', supportStatus: 'legacy-frozen', author: AUTHOR, ...entry
}))

const projects = [
  {
    loader: 'fabric', minecraftVersion: '1.21.11', version: '1.2.4+1.21.11', runtime: 25, gradle: 9,
    project: 'game-companions/fabric-1.21.11', filename: 'namlauncher-game-companion-fabric-1.21.11-1.2.4+1.21.11.jar'
  },
  {
    loader: 'forge', minecraftVersion: '1.21.11', version: '1.2.4+1.21.11', runtime: 21, gradle: 9,
    project: 'game-companions/forge-1.21.11', filename: 'namlauncher-game-companion-forge-1.21.11-1.2.4+1.21.11.jar'
  },
  {
    loader: 'neoforge', minecraftVersion: '1.21.11', version: '1.2.4+1.21.11', runtime: 21, gradle: 8,
    project: 'game-companions/neoforge-1.21.11', filename: 'namlauncher-game-companion-neoforge-1.21.11-1.2.4+1.21.11.jar'
  },
  {
    loader: 'fabric', minecraftVersion: '26.2', version: '1.2.4+26.2', runtime: 25, gradle: 9,
    project: 'game-bridge', filename: 'namlauncher-branding-bridge-1.2.4+26.2.jar', task: 'canonicalizeBridgeJar',
    minimumLoaderVersion: '0.19.3'
  },
  {
    loader: 'forge', minecraftVersion: '26.2', version: '1.2.4+26.2', runtime: 25, gradle: 9,
    project: 'game-companions/forge-26.2', filename: 'namlauncher-game-companion-forge-26.2-1.2.4+26.2.jar'
  },
  {
    loader: 'neoforge', minecraftVersion: '26.2', version: '1.2.4+26.2', runtime: 25, gradle: 9,
    project: 'game-companions/neoforge-26.2', filename: 'namlauncher-game-companion-neoforge-26.2-1.2.4+26.2.jar'
  }
]

const assertWithin = (candidate, parent, label) => {
  const relative = path.relative(parent, candidate)
  if (!relative || relative.startsWith('..') || path.isAbsolute(relative)) {
    throw new Error(`${label} must resolve to a child of ${parent}`)
  }
}

const javaHomeFor = (runtime) => {
  const architecture = process.arch === 'arm64' ? 'ARM64' : 'X64'
  const configured = process.env[`NAMLAUNCHER_JAVA${runtime}_HOME`]
    || process.env[`JAVA_HOME_${runtime}_${architecture}`]
    || (runtime === 25 ? process.env.JAVA_HOME : '')
  if (configured) return path.resolve(configured)
  if (process.platform === 'win32') {
    const candidates = runtime === 17
      ? ['C:/Users/novic/.gradle/namlauncher-toolchains/jdk-17-unpacked/jdk-17.0.20.1+1']
      : runtime === 21
        ? ['C:/Users/novic/.gradle/namlauncher-toolchains/jdk-21-unpacked/jdk-21.0.12.1+1']
        : ['C:/Program Files/Java/jdk-25.0.3', 'C:/Program Files/Java/latest']
    return path.resolve(candidates[0])
  }
  throw new Error(`Set NAMLAUNCHER_JAVA${runtime}_HOME before preparing game companions.`)
}

const executable = (home, name) => path.join(home, 'bin', process.platform === 'win32' ? `${name}.exe` : name)

const gradle8Home = () => {
  const configuredHome = process.env.NAMLAUNCHER_GRADLE8_HOME
  if (configuredHome) return path.resolve(configuredHome)
  const configuredCommand = process.env.NAMLAUNCHER_GRADLE8
  if (configuredCommand) return path.resolve(configuredCommand, '..', '..')
  if (process.platform === 'win32') {
    return 'C:/Users/novic/.gradle/namlauncher-toolchains/gradle-8.8'
  }
  throw new Error('Set NAMLAUNCHER_GRADLE8_HOME before preparing legacy game companions.')
}

const run = (command, args, options) => {
  const result = spawnSync(command, args, {
    ...options,
    encoding: 'utf8',
    shell: false,
    stdio: 'inherit',
    windowsHide: true
  })
  if (result.error) throw result.error
  if (result.status !== 0) throw new Error(`${path.basename(command)} failed with exit code ${result.status}`)
}

const buildProject = (entry, taskOverride) => {
  const projectRoot = path.join(WORKSPACE_ROOT, entry.project)
  assertWithin(projectRoot, WORKSPACE_ROOT, 'Game companion project')
  const javaHome = javaHomeFor(entry.runtime)
  const env = { ...process.env, JAVA_HOME: javaHome, PATH: `${path.join(javaHome, 'bin')}${path.delimiter}${process.env.PATH || ''}` }
  const tasks = taskOverride ? [taskOverride] : [
    ...(process.argv.includes('--incremental') ? [] : ['clean']), entry.task || 'build'
  ]
  const diagnostics = ['--no-daemon', '--console=plain', '--stacktrace']
  if (process.argv.includes('--low-memory')) diagnostics.push('-Dorg.gradle.jvmargs=-Xmx1G')
  if (entry.gradle === 8) {
    const launcherJar = path.join(gradle8Home(), 'lib', 'gradle-launcher-8.8.jar')
    if (!existsSync(launcherJar)) throw new Error(`Gradle 8.8 launcher was not found at ${launcherJar}`)
    run(executable(javaHome, 'java'), [
      '-classpath', launcherJar, 'org.gradle.launcher.GradleMain', '-p', projectRoot, ...tasks, ...diagnostics
    ], { cwd: WORKSPACE_ROOT, env })
    return
  }
  run(executable(javaHome, 'java'), [
    '-classpath', WRAPPER_JAR, 'org.gradle.wrapper.GradleWrapperMain', '-p', projectRoot, ...tasks, ...diagnostics
  ], { cwd: WORKSPACE_ROOT, env })
}

const digest = async (filePath) => createHash('sha256').update(await readFile(filePath)).digest('hex')
const digestBytes = (bytes) => createHash('sha256').update(bytes).digest('hex')

const atomicCopy = async (source, target) => {
  const temporary = `${target}.${process.pid}.tmp`
  await rm(temporary, { force: true })
  await copyFile(source, temporary)
  await rm(target, { force: true })
  await rename(temporary, target)
}

const atomicWrite = async (target, content) => {
  const temporary = `${target}.${process.pid}.tmp`
  await rm(temporary, { force: true })
  await writeFile(temporary, content, { encoding: 'utf8', flag: 'wx' })
  await rm(target, { force: true })
  await rename(temporary, target)
}

const verifyMetadata = (filePath, loader, expectedIconSha256, maintained = false) => {
  const archive = new AdmZip(filePath)
  const iconEntry = archive.getEntry(MOD_ICON_ENTRY)
  if (!iconEntry || iconEntry.isDirectory) {
    throw new Error(`${path.basename(filePath)} is missing the NamLauncher mod icon`)
  }
  const iconBytes = archive.readFile(iconEntry)
  if (!iconBytes || digestBytes(iconBytes) !== expectedIconSha256) {
    throw new Error(`${path.basename(filePath)} does not contain the canonical NamLauncher mod icon`)
  }
  const protectedIcon = archive.readFile('namlauncher-internal/badge.png')
  if (!protectedIcon || digestBytes(protectedIcon) !== expectedIconSha256) {
    throw new Error(`${path.basename(filePath)} is missing the protected badge icon`)
  }
  const mixinEntry = archive.getEntries().find((entry) => entry.entryName.endsWith('.mixins.json'))
  const mixins = mixinEntry ? JSON.parse(archive.readAsText(mixinEntry)) : null
  if (!mixins?.client?.includes('BadgeResourceManagerMixin') || mixins.required !== false || mixins.injectors?.defaultRequire !== 0) {
    throw new Error(`${path.basename(filePath)} must protect badge resources without mandatory cosmetic injections`)
  }
  if (maintained) {
    if (!mixins.client.includes('ClientPacketListenerMixin')) {
      throw new Error(`${path.basename(filePath)} is missing the /namlauncher client command`)
    }
    const fontEntry = archive.getEntry('assets/namlauncher/font/badge.json')
    const font = fontEntry && !fontEntry.isDirectory ? JSON.parse(archive.readAsText(fontEntry, 'utf8')) : null
    const provider = Array.isArray(font?.providers) ? font.providers[0] : null
    if (provider?.file !== 'namlauncher:font/badge.png' || provider?.height !== 8 || provider?.ascent !== 8) {
      throw new Error(`${path.basename(filePath)} has an invalid protected badge font fallback`)
    }
  }

  if (loader === 'fabric') {
    const entry = archive.getEntry('fabric.mod.json')
    if (!entry || entry.isDirectory) throw new Error(`${path.basename(filePath)} is missing fabric.mod.json`)
    const metadata = JSON.parse(archive.readAsText(entry, 'utf8'))
    if (metadata.id !== 'namlauncher-game-companion' && metadata.id !== 'namlauncher-branding-bridge') {
      throw new Error(`${path.basename(filePath)} has an unexpected Fabric mod id`)
    }
    if (metadata.icon !== MOD_ICON_ENTRY) {
      throw new Error(`${path.basename(filePath)} does not declare the canonical Fabric mod icon`)
    }
    return
  }
  const entryName = loader === 'forge' ? 'META-INF/mods.toml' : 'META-INF/neoforge.mods.toml'
  const entry = archive.getEntry(entryName)
  const metadata = entry && !entry.isDirectory ? archive.readAsText(entry, 'utf8') : ''
  const logoFile = metadata.match(/^\s*logoFile\s*=\s*["']([^"'\r\n]+)["']\s*$/m)?.[1]
  if (!metadata || !/modId\s*=\s*["']namlauncher_game_companion["']/.test(metadata)) {
    throw new Error(`${path.basename(filePath)} has invalid ${entryName}`)
  }
  if (logoFile !== MOD_ICON_ENTRY) {
    throw new Error(`${path.basename(filePath)} does not declare the canonical mod logo`)
  }
}

const clearManagedStageFiles = async () => {
  await mkdir(STAGE_DIR, { recursive: true })
  const stageInfo = await lstat(STAGE_DIR)
  if (!stageInfo.isDirectory() || stageInfo.isSymbolicLink()) {
    throw new Error(`Destination must be a real directory: ${STAGE_DIR}`)
  }
  const stageReal = await realpath(STAGE_DIR)
  if (stageReal === path.parse(stageReal).root) {
    throw new Error('Refusing to use a filesystem root as the launcher bundle destination')
  }
  for (const name of await readdir(stageReal)) {
    if (!MANAGED_STAGE_FILE.test(name)) continue
    const candidate = path.join(stageReal, name)
    assertWithin(candidate, stageReal, 'Managed staged file')
    const info = await lstat(candidate)
    if (!info.isFile() || info.isSymbolicLink()) throw new Error(`Refusing to replace unsafe staged path ${candidate}`)
    await rm(candidate, { force: true })
  }
  return stageReal
}

const purgeProjectCache = async (entry) => {
  const projectReal = await realpath(path.join(WORKSPACE_ROOT, entry.project))
  const cachePath = path.join(projectReal, '.gradle')
  try {
    const cacheReal = await realpath(cachePath)
    assertWithin(cacheReal, projectReal, 'Project Gradle cache')
    await rm(cacheReal, { recursive: true, force: true })
  } catch (error) {
    if (!error || typeof error !== 'object' || error.code !== 'ENOENT') throw error
  }
}

const main = async () => {
  const expectedIconSha256 = await digest(MOD_ICON_SOURCE)
  for (const entry of retainedArtifacts) {
    const retainedPath = path.join(LEGACY_DIR, entry.filename)
    const info = await lstat(retainedPath)
    if (!info.isFile() || info.isSymbolicLink() || info.size !== entry.size
      || await digest(retainedPath) !== entry.sha256) {
      throw new Error(`Frozen legacy companion failed integrity verification: ${entry.filename}`)
    }
    verifyMetadata(retainedPath, entry.loader, expectedIconSha256)
  }

  if (!process.argv.includes('--skip-build')) {
    for (const entry of projects) buildProject(entry)
  }

  // Validate the entire candidate set before replacing any previous staged artifact.
  for (const entry of projects) {
    const candidate = await realpath(path.join(WORKSPACE_ROOT, entry.project, 'build', 'libs', entry.filename))
    assertWithin(candidate, path.join(WORKSPACE_ROOT, entry.project), 'Built game companion')
    verifyMetadata(candidate, entry.loader, expectedIconSha256, true)
  }
  const stageReal = await clearManagedStageFiles()
  const artifacts = []
  for (const entry of retainedArtifacts) {
    const source = path.join(LEGACY_DIR, entry.filename)
    const target = path.join(stageReal, entry.filename)
    assertWithin(target, stageReal, 'Staged legacy game companion')
    await atomicCopy(source, target)
    if (await digest(target) !== entry.sha256) {
      throw new Error(`Staged legacy digest mismatch for ${entry.filename}`)
    }
    artifacts.push(entry)
  }
  for (const entry of projects) {
    const source = path.join(WORKSPACE_ROOT, entry.project, 'build', 'libs', entry.filename)
    const sourceReal = await realpath(source)
    assertWithin(sourceReal, path.join(WORKSPACE_ROOT, entry.project), 'Built game companion')
    const info = await stat(sourceReal)
    if (!info.isFile() || info.size <= 0 || info.size > MAX_ARTIFACT_BYTES) throw new Error(`Invalid artifact size for ${entry.filename}`)
    verifyMetadata(sourceReal, entry.loader, expectedIconSha256, true)
    const sha256 = await digest(sourceReal)
    const target = path.join(stageReal, entry.filename)
    assertWithin(target, stageReal, 'Staged game companion')
    await atomicCopy(sourceReal, target)
    if ((await digest(target)) !== sha256) throw new Error(`Staged digest mismatch for ${entry.filename}`)
    artifacts.push({
      id: 'namlauncher-game-companion',
      version: entry.version,
      launcherVersion: LAUNCHER_VERSION,
      loader: entry.loader,
      ...(entry.minimumLoaderVersion ? { minimumLoaderVersion: entry.minimumLoaderVersion } : {}),
      minecraftVersion: entry.minecraftVersion,
      supportStatus: 'maintained',
      environment: 'client',
      filename: entry.filename,
      sha256,
      size: info.size,
      author: AUTHOR
    })
    console.log(`Staged ${entry.loader} ${entry.minecraftVersion}: ${entry.filename} (${sha256})`)
  }

  await atomicWrite(path.join(stageReal, 'game-bridge-manifest.json'), `${JSON.stringify({
    schemaVersion: 2,
    launcherVersion: LAUNCHER_VERSION,
    artifacts,
    author: AUTHOR
  }, null, 2)}\n`)

  const stagedBundleFiles = (await readdir(stageReal))
    .filter((name) => name === 'game-bridge-manifest.json' || name.toLowerCase().endsWith('.jar'))
    .sort()
  const expectedBundleFiles = ['game-bridge-manifest.json', ...artifacts.map((entry) => entry.filename)].sort()
  if (JSON.stringify(stagedBundleFiles) !== JSON.stringify(expectedBundleFiles)) {
    throw new Error(`Unexpected files in game companion stage: ${stagedBundleFiles.join(', ')}`)
  }

  if (process.argv.includes('--clean-after-stage')) {
    for (const entry of projects) {
      buildProject(entry, 'clean')
      await purgeProjectCache(entry)
    }
  }
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : String(error))
  process.exitCode = 1
})
