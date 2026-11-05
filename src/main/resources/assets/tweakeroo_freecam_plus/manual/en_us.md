**In short**

- In Tweakeroo's free camera, middle-click a block to mark it; arrows lead you there once you are back in your body.
- Hold Ctrl and scroll to fly faster or slower (x1 to x64).
- Tap Ctrl three times to see where your character is.
- Left and right click work on what the camera looks at (within your character's reach); placed blocks face the camera's way (Minecraft 1.21.11).
- Client-side only, works as soon as it is installed. Needs Tweakeroo: the table under Requirements shows which version for your Minecraft.

A small client-side add-on for the **Free Camera** of Tweakeroo. Scout a spot with the free camera, mark it with the middle mouse button, and let arrows lead you there once you are back in your body. Hold Ctrl and scroll to fly faster or slower, and tap Ctrl three times when you lose track of where you left your character.

Install it, and Tweakeroo's free camera has these extras. On 1.21.11 a small settings screen (Mod Menu) adds two lock keys and "Leave When Hurt".

## Features

### Waypoints

- **Set a waypoint with the middle mouse button.** While the free camera is on, press the Pick Block key (middle mouse button by default) to put a waypoint on the block under your crosshair, up to 256 blocks away. If no block is in reach, for example when you look at the sky, the waypoint goes where the camera is. The action bar confirms it with the waypoint's number and coordinates.
- **Remove it the same way.** Middle-click while looking at a waypoint, or very close to it, to remove it again.
- **Share it.** Shift + middle click on a waypoint (or /fcpwaypoint list for all dimensions) says it in chat in Xaero's Minimap format; Xaero's Minimap and Conflux Map users can add it, and waypoints they share get an **[Add waypoint]** for you.
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

- **Hurt alert.** When your character gets hurt while you are in the free camera you hear a bell and see a red line with its health (on 1.21.11 it can also switch the free camera off).
- **Triple-tap Ctrl.** Tap Sprint three times quickly in the free camera. For 5 seconds your character gets a cyan outline that shows through walls, and a cyan arrow on the crosshair ring points at it with the distance.

### Using things from the free camera (1.21.11)

- **Left and right click from the camera.** Tweakeroo normally ignores clicks in the free camera. Now you break, place, open and use whatever the camera's crosshair is on, as long as your character could reach it (the server measures the reach from your character); farther blocks are not targeted.
- **Blocks face the camera's way.** Stairs, observers, pistons and other blocks that face a direction are placed as if your character looked the way the camera does; your character turns back right after.
- **Buckets and other aimed items** hit the block the camera looks at.
- **Sneak for one click.** Sneak moves the camera down, but holding it while you click sneaks your character for that click, so you can place blocks against chests and other blocks you could open.
- **Lock the placing direction (Ctrl + L).** Blocks keep the direction of your last placement (or the camera's, if you haven't placed yet) while the camera looks around. Press again to follow the camera again. The locked direction is shown under the sprint line.
- **Lock sneak for clicks (Ctrl + Right Shift).** Every click from the camera is a sneaking click until you press it again. Your character itself never sneaks: it does not crouch, and a flying character does not sink.
- **Both hotkeys can be changed** in the settings (Mod Menu → Tweakeroo Freecam Plus). They only act in the free camera; both locks end when you leave it.

### Toggle Sprint

- **No flickering.** If you use the vanilla "Sprint: Toggle" setting, the free camera now sprints while you actually hold Ctrl, instead of switching on and off while the key is held. As in Tweakeroo, sprint then stays on until you stop moving forward or backward.
- **Your character's sprint is left alone.** With Toggle Sprint, pressing Ctrl in the free camera no longer switches your character's sprint on or off. When you leave the free camera, it is exactly as you left it.

## How to use

Besides `/fcpwaypoint` (list, share and remove waypoints) and `/fcpmanual` (the in-game manual), the mod uses two vanilla keys, only while Tweakeroo's free camera is on:

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

On 1.21.11, Mod Menu → Tweakeroo Freecam Plus has three settings: **Leave When Hurt** (off), **Lock placing direction** (Left Ctrl + L) and **Lock sneak** (Left Ctrl + Right Shift); the screen also opens the manual. Other versions have no settings. These are the fixed values the mod uses:

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

Each Minecraft version has its own jar, and each jar works with exactly one Tweakeroo version:

| Minecraft | Jar | Tweakeroo | MaLiLib | Java |
|---|---|---|---|---|
| 1.21.5 | `tweakeroo-freecam-plus-1.0.0+1.21.5.jar` | 0.24.3 | 0.24.3 or newer within 0.24.x | 21 |
| 1.21.6, 1.21.7, 1.21.8 | `tweakeroo-freecam-plus-1.0.0+1.21.8.jar` | 0.25.6 | 0.25.6 or newer within 0.25.x | 21 |
| 1.21.9, 1.21.10 | `tweakeroo-freecam-plus-1.0.0+1.21.10.jar` | 0.26.5 | 0.26.4 or newer within 0.26.x | 21 |
| 1.21.11 | `tweakeroo-freecam-plus-1.1.0+1.21.11.jar` | 0.27.15 | 0.27.19 or newer within 0.27.x | 21 |
| 26.1, 26.1.1, 26.1.2 | `tweakeroo-freecam-plus-1.0.0+26.1.2.jar` | 0.28.10 | 0.28.11 or newer within 0.28.x | 25 |
| 26.2 | `tweakeroo-freecam-plus-1.0.0+26.2.jar` | 0.29.5 | 0.29.5 or newer within 0.29.x | 25 |
| 26.3 | `tweakeroo-freecam-plus-1.0.0+26.3.jar` | 0.30.1 | 0.30.2 or newer within 0.30.x | 25 |

You also need Fabric Loader (0.17.0 or newer; 0.19.3 or newer on 26.x) and Fabric API for your Minecraft version.

Client-side only. The server does not need it, so it works in singleplayer and on servers.

## Compatibility

- **Tweakeroo version.** This mod changes how Tweakeroo's free camera moves, so each jar is tied to one Tweakeroo version (see the table under Requirements). With any other Tweakeroo version, Fabric Loader stops the game at startup and tells you which version is needed.
- **Other free camera mods.** Only Tweakeroo's Free Camera gets the extras. Other free camera mods are not affected.
- **Pick Block in the free camera.** While the free camera is on, the Pick Block key sets waypoints and does not pick blocks. Outside the free camera it works as usual.
- **Ctrl + mouse wheel.** The wheel is only taken over while the free camera is on and Sprint is held. If another mod also uses Ctrl + mouse wheel, the two can get in each other's way in that moment.
- **Sprint on a mouse button.** Ctrl + wheel works with any Sprint binding, but the triple tap only works when Sprint is bound to a keyboard key.
- **Sodium, Iris and other rendering mods.** There are no compatibility notes yet. If waypoint boxes or beams do not show up, please open an issue.

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

## Newer features and settings

### Settings (Minecraft 1.21.11)

Open them with Mod Menu → Tweakeroo Freecam Plus → the settings button (the same screen has this manual).

| Setting | Default | What it does |
|---|---|---|
| Leave When Hurt | off | When your character gets hurt while you are in the free camera, the free camera is switched off and a temporary waypoint is left where the camera was. |
| Lock placing direction | Left Ctrl + L | In the free camera, blocks keep the direction of your last placement while the camera looks around. Press again to follow the camera again. |
| Lock sneak | Left Ctrl + Right Shift | In the free camera, every click from the camera is a sneaking click (places against chests instead of opening them) until you press it again. Your character does not crouch. |

On the other Minecraft versions there is no settings screen; the two lock keys and "Leave When Hurt" exist only on 1.21.11. Open this manual there with `/fcpmanual`.

### Hurt alert

While you are in the free camera and your character gets hurt, a bell sounds and a red line shows its health, on every version. With "Leave When Hurt" on (1.21.11), the free camera also switches off and a temporary waypoint marks where the camera was; it goes away when you reach it or when you switch the free camera on again.

### Sharing waypoints

- **Shift + middle click** on a waypoint in the free camera says it in chat in Xaero's Minimap format. People with Xaero's Minimap or Conflux Map can add it to their map.
- Waypoints that others share (from Xaero's Minimap, Conflux Map or this mod) get an **[Add waypoint]** button in your chat, also for other dimensions.
- `/fcpwaypoint list` lists your waypoints of every dimension with **[Share]** and **[Remove]**; `/fcpwaypoint share <number>` and `/fcpwaypoint remove <number>` do the same by number. These commands run on your own game only; nothing is sent to the server.

## Where it works

- Only on your own game (client side), in singleplayer and on every server. Needs Tweakeroo; the features only work while Tweakeroo's Free Camera is on (the waypoints, arrows and alerts stay after you leave it).
- Waypoints are kept per world or server, at most 10.
