# Clean and Clear Glass - Fabric 1.16.5

Clean and Clear Glass is a client-side Fabric mod that gives vanilla glass blocks and glass panes a cleaner, connected appearance without adding or replacing blocks and items.

This branch is built only for Minecraft 1.16.5. Minecraft 1.16.5 has no tinted glass block, so this version intentionally provides only the Regular Glass style setting.

## Settings

Open the settings screen with `Alt + G`, or through Mod Menu when it is installed.

| Glass Type | Styles | Default |
|---|---|---|
| Regular Glass | Clear, Subtle | Clear |

Regular Glass keeps the same outer border in both styles. Subtle adds a separate center detail to placed blocks and panes.

## How It Works

The mod wraps the baked vanilla models for glass, the 16 stained glass colors, glass panes, and stained glass panes. Matching neighbors hide their internal borders while exposed faces, pane caps, and pane intersections remain visible. Held and inventory items switch between the clean and original vanilla models with the same ON/OFF setting.

Fabric API by itself uses Indigo. Sodium 0.2.0 build 4 uses a dedicated direct backend: clear glass remains in Sodium's fast chunk path, while partially transparent stained glass is drawn through a camera-sorted glass pass. This avoids the close-range SOUTH/WEST disappearance caused by Sodium's fixed quad order. Indium is optional and does not replace this backend.

Regular Glass Subtle applies its center layer only to the broad visible pane surfaces. Thin sides, endpoint caps, and borders continue to use the regular glass edge texture.

This integration intentionally targets Minecraft 1.16.5, Fabric API 0.42.0, and Sodium 0.2.0 build 4 only. Indium and Continuity are not required.

## Requirements

- Minecraft 1.16.5
- Fabric Loader 0.14.0 or newer
- Fabric API 0.42.0+1.16
- Java 8 for Minecraft, with Java 17 used by the development build and `runClient` tasks

## License

Licensed under the [Lidwinae Mod License v1.0](LICENSE).
