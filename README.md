# Pack Editor (Fabric, Minecraft 26.2)

Client-side Fabric mod. Press **O** in game to pick a look for end crystals, respawn anchors,
totems, ender pearls, golden apples, shields, obsidian, glowstone, cobwebs, fire, swords, axes,
pickaxes, mace and diamond/netherite armour.

**Looks:** vanilla, 12 styles (Crimson, Frost, Toxic, Royal, Gilded, Sakura, Ocean, Midnight, Neon,
Pastel, Monochrome, Inverted), a custom hex colour, colours copied from a **screenshot you drop onto the
window** (or paste its path), and textures from your own resource packs in `.minecraft/resourcepacks`.
**Configs:** presets, save/load your own setups, copy/paste share codes (`PE1-...`).

Apply writes a resource pack to `resourcepacks/packeditor_generated/`, enables it and reloads.
Delete that folder (or untick it in Resource Packs) to go back to vanilla.

## Build

Push this folder to a GitHub repo, then Actions -> "Build mod jar" -> Run workflow.
The jar is attached to the run as the `packeditor-26.2` artifact. Put it in `.minecraft/mods`
with Fabric Loader 0.19.5+ and Fabric API for 26.2 (Java 25).

Local: `./gradlew build` -> `build/libs/packeditor-1.0.0.jar`.

## Layout

- `src/main/java/dev/packeditor/core` - all logic with no Minecraft code (recolouring, styles, pack
  writer, share codes, config). Tested standalone.
- `src/client/java/dev/packeditor/client` - the game-facing layer. Anything that touches Minecraft's
  API is in `Draw.java` (drawing), `Editor.java` (clipboard, screen opening, enabling the pack),
  `McTextureSource.java` (reading vanilla textures) and `PackEditorClient.java` (key binding).
  If a 26.2 build error mentions a Minecraft class or method, it will be in one of those or in the
  screens under `client/screen`.

## Credit

Feature set inspired by Awerial's Pack Editor (MIT) for 1.21.11. This is a clean reimplementation;
no code or assets were copied. Not affiliated with Awerial, Mojang or Microsoft.
