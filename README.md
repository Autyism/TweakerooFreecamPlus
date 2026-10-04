<p align="center"><img src="docs/icon.png" width="128" alt="icon"></p>
<h1 align="center">Tweakeroo Freecam Plus</h1>
<p align="center">Waypoints, adjustable sprint speed and a pointer back to your character for Tweakeroo's free camera.</p>

**English** | [简体中文](README.zh-CN.md)

![Minecraft 1.21.11](https://img.shields.io/badge/Minecraft-1.21.11-62B47A) ![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4) ![Tweakeroo 0.27.15](https://img.shields.io/badge/Tweakeroo-0.27.15-E5A54B) ![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue)

A small client-side add-on for the **Free Camera** of [Tweakeroo](https://modrinth.com/mod/tweakeroo). Scout a spot with the free camera, mark it with the middle mouse button, and let arrows lead you there once you are back in your body. Hold Ctrl and scroll to fly faster or slower, and tap Ctrl three times when you lose track of where you left your character.

There are no settings and no switches. Install it, and Tweakeroo's free camera has these extras.

## Features

### Waypoints

- **Set a waypoint with the middle mouse button.** While the free camera is on, press the Pick Block key (middle mouse button by default) to put a waypoint on the block under your crosshair, up to 256 blocks away. If no block is in reach, for example when you look at the sky, the waypoint goes where the camera is. The action bar confirms it with the waypoint's number and coordinates.
- **Remove it the same way.** Middle-click while looking at a waypoint, or very close to it, to remove it again.
- **Visible through walls.** Every waypoint is drawn as a pink box around its block with a beam rising 24 blocks above it. Both show through terrain, in the free camera and in your normal view.
- **Arrows lead the way.** Small arrows on a ring around your crosshair point towards each waypoint, labelled with its number and distance in blocks. A tag tells you when it is more than 3 blocks above or below you. The arrows stay after you leave the free camera, so you can walk to the spot you scouted.
- **Cleared on arrival.** A waypoint disappears by itself once your character is within 3 blocks of it.
- **Remembered per world.** Waypoints are saved for each singleplayer world and each server address, and they are back when you return. You can have up to 10 at a time per world or server; an 11th replaces the oldest. Only the waypoints of the dimension you are in are shown.

### Sprint speed

- **Ctrl + mouse wheel changes the speed.** In the free camera, hold your Sprint key (Left Ctrl by default) and scroll up or down to step through x1, x1.5, x2, x3, x4, x6, x8, x12, x16, x24, x32, x48 and x64. It starts at x3, Tweakeroo's own sprint speed.
- **The hotbar stays put.** While Sprint is held in the free camera, the wheel only changes the speed, and your selected hotbar slot does not move.
- **Back to normal on every toggle.** The speed returns to x3 whenever you switch the free camera on or off, so a new flight never starts at a speed you forgot about.
- **Sprint indicator.** While the free camera is on, a small line at the top of the screen shows whether the camera is sprinting and the current speed, for example `Sprint ON  x6` in green or `Sprint OFF  x3` in grey.

### Find your character

- **Triple-tap Ctrl.** Tap Sprint three times quickly in the free camera. For 5 seconds your character gets a cyan outline that shows through walls, and a cyan arrow on the crosshair ring points at it with the distance.

### Toggle Sprint

- **No flickering.** If you use the vanilla "Sprint: Toggle" setting, the free camera now sprints while you actually hold Ctrl, instead of switching on and off while the key is held. As in Tweakeroo, sprint then stays on until you stop moving forward or backward.
- **Your character's sprint is left alone.** With Toggle Sprint, pressing Ctrl in the free camera no longer switches your character's sprint on or off. When you leave the free camera, it is exactly as you left it.

## Screenshots

![Setting a waypoint](docs/images/waypoint-set.png)

Setting a waypoint from the free camera. A pink box and beam mark the block, and the action bar shows the waypoint's number and coordinates.

![Arrows in your normal view](docs/images/waypoint-arrows.png)

Back in your body, the beams and the arrows around the crosshair lead you to each waypoint.

![Pointer to your character](docs/images/find-your-character.png)

After a triple tap on Ctrl, a cyan arrow points to your own character and shows the distance.

![Sprint indicator](docs/images/sprint-indicator.png)

The sprint indicator at the top of the screen while flying in the free camera.

![Sprint speed x6](docs/images/sprint-speed.png)

Two scrolls up with Ctrl held: the sprint speed is now x6.

## How to use

The mod adds no key bindings, commands or settings screen of its own. It uses two vanilla keys, and only while Tweakeroo's free camera is on:

| Action | Default key | Where to change it |
|---|---|---|
| Set or remove a waypoint (in the free camera) | Middle mouse button (Pick Block) | Options → Controls → Key Binds → Pick Block |
| Change the sprint speed (in the free camera) | Hold Left Ctrl (Sprint) + mouse wheel | Options → Controls → Key Binds → Sprint |
| Point at your character for 5 seconds (in the free camera) | Tap Left Ctrl (Sprint) three times quickly | Options → Controls → Key Binds → Sprint |
| Switch the free camera on or off | No key by default (Tweakeroo's Free Camera tweak) | Tweakeroo's menu (X + C) → Tweaks → Free Camera (`tweakFreeCamera`) |

Outside the free camera, Pick Block, Sprint and the mouse wheel work as usual.

### Mark a spot and walk to it

1. Switch on Tweakeroo's Free Camera.
2. Fly to the spot and look at the block you want to mark.
3. Press the middle mouse button. A pink box and beam appear, and the action bar shows the waypoint's number and coordinates.
4. Switch the free camera off and follow the arrow around your crosshair, or the beam.
5. When you are within 3 blocks, the waypoint removes itself. To remove it earlier, look at it in the free camera and middle-click again.

### Fly faster or slower

1. In the free camera, hold Ctrl.
2. Scroll up for more speed, down for less. The line at the top of the screen shows the current value.
3. Fly forward while holding Ctrl to sprint at that speed. Sprint stays on until you stop moving forward or backward.
4. Switching the free camera off and on again sets the speed back to x3.

### Find your character

In the free camera, tap Ctrl three times within about 0.7 seconds. For 5 seconds your character is outlined in cyan, and an arrow on the crosshair ring shows its direction and distance.

## Settings

There is nothing to configure: no options screen and no config file. For reference, these are the fixed values the mod uses:

| Name | Value | What it does |
|---|---|---|
| Waypoint reach | 256 blocks | How far the middle click looks for a block. |
| Waypoints per world or server | 10 | An 11th waypoint replaces the oldest one. |
| Arrival distance | 3 blocks | A waypoint is removed once your character is this close. |
| Above / below tag | more than 3 blocks | When the arrow label says the waypoint is above or below you. |
| Sprint speeds | x1 to x64 (13 steps) | The values Ctrl + mouse wheel steps through. |
| Starting speed | x3 | Tweakeroo's own sprint speed, used again after every toggle. |
| Triple tap | 3 taps within 0.7 seconds | Shows the pointer to your character. |
| Pointer time | 5 seconds | How long the pointer to your character stays visible. |

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.17.0 or newer |
| [Fabric API](https://modrinth.com/mod/fabric-api) | for 1.21.11 (built with 0.141.6) |
| [Tweakeroo](https://modrinth.com/mod/tweakeroo) | 0.27.15, exactly this version |
| [MaLiLib](https://modrinth.com/mod/malilib) | the version Tweakeroo 0.27.15 needs: 0.27.19 or newer within 0.27.x (built with 0.27.20) |
| Java | 21 |

Client-side only. The server does not need it, so it works in singleplayer and on servers.

## Compatibility

- **Tweakeroo version.** This mod changes how Tweakeroo's free camera moves, so it is tied to Tweakeroo 0.27.15. With any other Tweakeroo version, Fabric Loader stops the game at startup and tells you which version is needed.
- **Other free camera mods.** Only Tweakeroo's Free Camera gets the extras. Other free camera mods are not affected.
- **Pick Block in the free camera.** While the free camera is on, the Pick Block key sets waypoints and does not pick blocks. Outside the free camera it works as usual.
- **Ctrl + mouse wheel.** The wheel is only taken over while the free camera is on and Sprint is held. If another mod also uses Ctrl + mouse wheel, the two can get in each other's way in that moment.
- **Sprint on a mouse button.** Ctrl + wheel works with any Sprint binding, but the triple tap only works when Sprint is bound to a keyboard key.
- **Sodium, Iris and other rendering mods.** There are no compatibility notes yet. If waypoint boxes or beams do not show up, please [open an issue](https://github.com/Autyism/TweakerooFreecamPlus/issues).

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api), [MaLiLib](https://modrinth.com/mod/malilib), [Tweakeroo](https://modrinth.com/mod/tweakeroo) 0.27.15 and Tweakeroo Freecam Plus.
3. Put all four `.jar` files into the `mods` folder of your Minecraft directory (or of your launcher instance).
4. Start the game with the Fabric profile and switch on Tweakeroo's Free Camera.

## FAQ

**My middle click removes a waypoint instead of setting a new one.**

A middle click removes a waypoint that is within about 6 degrees of your crosshair. To set a new waypoint close to an existing one, fly closer, so that the two are further apart on screen.

**A waypoint I just set disappeared right away.**

Waypoints within 3 blocks of your character count as reached and are removed. This also happens when you set one right next to where your character stands.

**Changing the speed does nothing.**

The speed only applies while the camera sprints, and like Tweakeroo's own sprint it speeds up forward and backward flight only. Sideways and up/down movement keep their normal speed. It also has no effect while Tweakeroo's "Free Camera Player Movement" option is on, because then your movement keys move your character instead of the camera.

**Where are the waypoints stored?**

In `config/tweakeroo_freecam_plus/worlds/` inside your Minecraft folder, one file per world or server. To clear all waypoints of a world at once, delete its file while you are not in that world.

**Can other players see my waypoints?**

No. Waypoints only exist on your own screen; this mod sends nothing to the server.

## Known limitations

- The arrows show the horizontal direction only. Height is shown by the above/below tag.
- The sprint speed is not saved. It starts at x3 every time the free camera is switched on.
- Waypoints are stored by world folder name or server address. If either one changes, the old waypoints are not found.

## Credits

- [Tweakeroo](https://github.com/maruohon/tweakeroo) and [MaLiLib](https://github.com/maruohon/malilib) by masa, with Sakura-Ryoko, provide the free camera that this add-on extends. Tweakeroo Freecam Plus is an unofficial add-on by Autyism and is not affiliated with them.

## License

Tweakeroo Freecam Plus is licensed under the GNU General Public License v3.0 (GPL-3.0-only); see [LICENSE](LICENSE).
