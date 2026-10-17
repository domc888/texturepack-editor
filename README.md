# Pack Editor — Minecraft 26.2 (Fabric)

This is a source project and GitHub Actions JAR builder for a client-side Minecraft Fabric mod. The source currently in this project is a clean reimplementation inspired by the described Pack Editor feature set; it is **not a verified 1:1 port of `awerials-pack-editor-1.45.0.jar`**. The original JAR/source was not available in the working files used for this build, so matching every original screen and renderer hook cannot be verified.

## Open the editor

- Press **O** in game to open Pack Editor.
- Use **PvP**, **Blocks**, and **Gear** to choose textures, vanilla looks, one of 12 colour styles, custom hex colours, screenshot colour ramps, or textures from resource packs.
- Open **Configs** to save/load presets and personal setups or copy/paste setup share codes.
- Open **All Features** to access the effects, hitbox colours, inventory gradients, sky, switches, gear swapping, hats/capes, title screen and pixel drawing pages.
- The title screen has a **Pack Editor** button when that option is enabled.
- **G** swaps the armour set in the configured hotbar slots with worn armour when both `Gear Swap` and `Swap Worn Armour` are enabled.

## Features implemented in this source

- Texture recolouring and presets for end crystals, respawn anchors, totems, pearls, golden apples, shields, blocks, swords, axes, pickaxes, mace and diamond/netherite armour.
- Custom colours, screenshot-derived colour ramps, resource-pack texture selection, saved setups and share codes.
- A 16×16 drawing editor with four frames, pencil, eraser, flood fill, colour picker, mirror drawing, a grid and undo. Save + Apply exports a standalone resource pack with Minecraft animation metadata for the selected target texture.
- Totem-count HUD, health-damage tint, durability warning, hit/kill particles, mace/lunge rings, and client-side end-crystal outlines.
- Resource-pack transformations for low-fire textures, crosshair scaling, glint tint, title-screen panorama tinting, supported hotbar/inventory gradients and slot tinting.
- Persistent per-feature settings, title-screen shortcut and armour-slot swapping.

## Options that are exposed but not fully implemented yet

Some sections are present in the settings UI but still need dedicated Minecraft renderer/model hooks before they affect gameplay visuals: custom hitbox outline colours, 3D hat/cape geometry, first-person hand transforms, 3D world/Nether/End sky tinting and stars, shield-size transforms, and selective explosion-particle suppression. These settings are saved, but the current source does not yet provide those missing hooks. They should not be treated as working features in this build.

## Generated files

Press **Apply** to write the generated resource pack under `.minecraft/resourcepacks/packeditor_generated/` and activate it. Drawing packs are saved separately under `.minecraft/resourcepacks/PackEditor-Drawing-*`. The drawing drafts and visual feature settings are stored in the Minecraft config directory.

## Build a JAR with GitHub Actions

1. Upload the contents of this ZIP to the **root** of a GitHub repository, including the `.github` folder.
2. Push to `main` or `master`, or open **Actions → Build mod jar → Run workflow**.
3. Download the `packeditor-26.2` artifact from that run. It contains the mod JAR; do not install a `*-sources.jar` file.
4. Use a Minecraft 26.2 instance with Fabric Loader, Fabric API and Java 25.

The workflow is `.github/workflows/build.yml`. It runs `./gradlew build --no-daemon --stacktrace` and uploads the JAR from `build/libs/`.

## Local build

Run `./gradlew build` on a system with network access and Java 25. This environment does not have Java 25 and cannot reach Gradle's distribution server, so a complete Minecraft/Fabric build was not verified here. The plain-Java core and the settings/drawing/resource-pack writer classes were compiled locally with Java 21; that does **not** prove that the game-facing code compiles against 26.2.

## Project layout

- `src/main/java/dev/packeditor/core/` — plain-Java recolouring, textures, target definitions, presets, resource-pack writing and config/share-code logic.
- `src/client/java/dev/packeditor/client/` — Minecraft client entrypoint, GUI screens, feature hooks, HUD, combat effects and drawing editor.
- `src/main/resources/fabric.mod.json` — Fabric metadata.
- `.github/workflows/build.yml` — JAR builder.

## Credit

Feature inspiration: Awerial's Pack Editor. This project is an independent clean reimplementation, with no original code or assets copied, and is not affiliated with Awerial, Mojang or Microsoft.
