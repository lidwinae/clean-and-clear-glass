# Clean and Clear Glass - Fabric 1.20.1

Clean and Clear Glass is a client-side Fabric mod that gives vanilla glass blocks and glass panes a cleaner, connected appearance without adding or replacing any blocks or items.

This branch is built only for Minecraft 1.20.1.

## Settings

Open the settings screen with `Alt + G`, or through Mod Menu when it is installed.

| Glass Type | Styles | Default |
|---|---|---|
| Regular Glass | Clear, Subtle | Clear |
| Tinted Glass | Subtle, Visible, Clear | Subtle |

Regular Glass keeps the same outer border in both styles. Subtle adds a separate center detail to placed blocks and panes. Tinted Glass changes both its placed appearance and item appearance.

## How It Works

The mod wraps Minecraft's baked vanilla glass models on the client. For placed blocks and panes, it checks adjacent matching glass and renders only the exposed faces, caps, borders, and pane intersections. Internal borders between matching neighbors are hidden while outer borders stay visible.

Without Sodium, connected geometry is emitted through Fabric Renderer API and Indigo. When Sodium 0.5.13 is installed, clear glass uses the direct baked-quad chunk backend. Partially transparent stained and tinted glass is removed from Sodium's fixed-order chunk buffers and drawn in a camera-sorted glass pass. This prevents nearby layered glass from disappearing on the SOUTH/WEST views while preserving its real alpha values.

The direct Sodium path is also preferred when Indium is installed, so Fabric API with Sodium and Fabric API with Sodium plus Indium use the same C&CG renderer. Indium and Continuity are not required. The integration intentionally targets Sodium 0.5.13 for Minecraft 1.20.1 only; unsupported Sodium versions safely fall back to vanilla block geometry.

Regular Glass Subtle applies its center layer only to the broad visible pane surfaces. Thin sides, endpoint caps, and borders continue to use the regular glass edge texture.

Held and inventory items are selected at render time. When Clean and Clear Glass is disabled, Minecraft's original block and item models are used.

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.16.10 or newer
- Fabric API 0.92.11+1.20.1 or newer compatible release
- Java 17

## License

Licensed under the [Lidwinae Mod License v1.0](LICENSE).
