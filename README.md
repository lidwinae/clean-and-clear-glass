# Clean and Clear Glass (C&CG)

<table>
  <tr>
    <td><strong>GitHub Releases</strong></td>
    <td><a href="https://github.com/lidwinae/clean-and-clear-glass/releases">https://github.com/lidwinae/clean-and-clear-glass/releases</a></td>
  </tr>
  <tr>
    <td><strong>Google Sites</strong></td>
    <td><a href="https://sites.google.com/view/lidwinae/mods/clean-and-clear-glass">https://sites.google.com/view/lidwinae/mods/clean-and-clear-glass</a></td>
  </tr>
  <tr>
    <td><strong>CurseForge</strong></td>
    <td><a href="https://www.curseforge.com/minecraft/mc-mods/clean-and-clear-glass">https://www.curseforge.com/minecraft/mc-mods/clean-and-clear-glass</a></td>
  </tr>
  <tr>
    <td><strong>Modrinth</strong></td>
    <td><a href="https://modrinth.com/mod/clean-and-clear-glass">https://modrinth.com/mod/clean-and-clear-glass</a></td>
  </tr>
</table>

Clean and Clear Glass, or C&CG, is a Fabric client-side mod for Minecraft Java Edition 26.1.x that makes vanilla glass blocks and glass panes cleaner while keeping matching glass variants visually connected.

The mod keeps the original vanilla block IDs. It does not add new glass blocks or items.

## Features

- Connected rendering for vanilla glass blocks and panes
- Supports regular, stained, and tinted glass
- Configurable Regular Glass and Tinted Glass styles
- Keeps vanilla block and item IDs
- Standalone client-side rendering without CTM or Continuity

## Settings

Open the settings screen with:

```text
Alt + G
```

The key can be changed in:

```text
Options > Controls > Key Binds
```

The settings screen can also be opened through Mod Menu when Mod Menu is installed.

Available styles:

| Glass Type | Styles | Default |
|---|---|---|
| Regular Glass | Clear, Subtle | Clear |
| Tinted Glass | Subtle, Visible, Clear | Subtle |

Regular Glass keeps the same clean border in both styles. The Subtle style only adds small center marks to placed glass blocks and glass panes, so the regular glass and glass pane item appearances remain unchanged.

Tinted Glass changes both its placed block appearance and item appearance to match the selected style.

Applying settings reloads game resources so connected block and pane models can use the selected appearance.

## How It Works

![How Clean and Clear Glass works](docs/images/how-it-works.png)

Clean and Clear Glass wraps Minecraft's baked vanilla glass models on the client side. The renderer checks nearby matching glass, hides connected internal faces, and keeps the visible outer faces, caps, and edge strips.

Regular Glass Subtle uses a separate center layer while retaining the same border texture as Regular Glass Clear. Glass panes apply this center layer only to their broad visible surfaces, leaving their thin sides, endpoint caps, and outer borders unchanged.

When the mod is turned off, the wrapped models are skipped and Minecraft's original block and item models are used.

## Supported Version

- Minecraft Java Edition 26.1.x
- Fabric Loader 0.19.3 or newer
- Fabric API for Minecraft 26.1.x
- Java 25

## Installation

1. Install Fabric Loader for Minecraft 26.1.x.
2. Install the matching Fabric API version.
3. Put the mod `.jar` file into your `mods` folder.
4. Launch the game with the Fabric profile.

## License

Clean and Clear Glass is licensed under the **Lidwinae Mod License v1.0**.

See the included [LICENSE](LICENSE) file or the [canonical license text](https://github.com/lidwinae/lidwinae-mod-license/blob/v1.0/LICENSE.md) for the complete terms.

This license applies to the project's original materials. Minecraft and third-party assets remain subject to the rights of their respective owners.

This project is not an official Minecraft product and is not approved by or associated with Mojang Studios or Microsoft.
