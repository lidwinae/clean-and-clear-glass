# Clean and Clear Glass - NeoForge 1.21.11

Clean and Clear Glass is a standalone client-side mod that makes vanilla glass blocks and glass panes look clean, clear, and connected. It supports regular glass, stained glass, and tinted glass without adding new blocks, items, or recipes.

This version uses NeoForge's model system, not Fabric API. No separate resource pack, OptiFine, Continuity, or Indium is required.

## How it works

`GlassModelHandler` wraps the vanilla glass models when Minecraft loads its resources. The connected block and pane models check neighboring glass of the same variant, then use `NeoQuadEmitter` to build the textured faces, or quads, that Minecraft renders.

Connected internal borders are hidden while outer borders remain visible. Glass panes also account for horizontal connections, vertically stacked panes, exposed ends, and T-shaped or cross-shaped intersections.

Regular glass and regular glass panes have **Clear** (default) and **Subtle** styles. Both styles use the same item appearance. Tinted glass has **Subtle** (default), **Visible**, and **Clear** styles, with item textures that follow the selected style.

Turning the mod OFF and applying the settings restores vanilla block and item rendering. These changes affect appearance only; the original blocks and their gameplay properties remain unchanged.

## Settings

Press **Alt + G** in-game or open the configuration screen from NeoForge's Mods menu. Both the modifier and the main key can be changed under **Options > Controls > Key Binds**.

Applying settings saves the configuration and reloads resources to update the glass models and textures.

## Requirements

- Minecraft Java Edition **1.21.11**
- NeoForge **21.11.44 or newer for Minecraft 1.21.11**
- Java **21**

Licensed under the [Lidwinae Mod License v1.0](LICENSE.md).
