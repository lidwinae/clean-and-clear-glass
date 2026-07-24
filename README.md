# Clean and Clear Glass - Fabric 1.21.11

Clean and Clear Glass is a client-side Fabric mod that gives vanilla glass blocks and glass panes a cleaner, connected appearance without adding or replacing blocks and items.

This branch is built only for Minecraft 1.21.11.

## Settings

Open the settings screen with `Alt + G`, or through Mod Menu when it is installed.

| Glass Type | Styles | Default |
|---|---|---|
| Regular Glass | Clear, Subtle | Clear |
| Tinted Glass | Subtle, Visible, Clear | Subtle |

Regular Glass keeps the same outer border in both styles. Subtle adds a separate center detail to placed blocks and panes. Tinted Glass changes both its placed appearance and item appearance.

## How It Works

The mod wraps Minecraft's baked vanilla glass models on the client. It checks adjacent matching glass, removes connected internal faces and borders, and keeps exposed faces, pane caps, intersections, and outer borders visible.

Regular Glass Subtle applies its center layer only to the broad visible pane surfaces. Thin sides, endpoint caps, and borders continue to use the regular glass edge texture.

Held and inventory items are selected at render time. When the mod is disabled, Minecraft's original block and item models are used.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.17.3 or newer
- Fabric API for Minecraft 1.21.11
- Java 21

## License

Licensed under the [Lidwinae Mod License v1.0](LICENSE).
