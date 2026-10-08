plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11"

// src/ is always kept in the 1.21.11 state; never switch the active version.
// The renames below go one way: they are applied to the versions their condition is true for, and
// their reverse pattern "(?!)" matches nothing, so the 1.21.11 source is never touched by them.
// Only whole names that mean one thing in this code are renamed this way; everything else uses //? conditions.
val never = "(?!)"

stonecutter parameters {
    fun oneWay(condition: Boolean, vararg renames: Pair<String, String>) {
        for ((from, to) in renames) {
            replacements.regex(condition) { replace(from, to, never, to) }
        }
    }

    // 26.1: GUI drawing renamed (same arguments). These calls only ever go to GuiGraphics in this code.
    oneWay(current.parsed >= "26.1",
        "\\bGuiGraphics\\b" to "GuiGraphicsExtractor",
        "\\.drawString\\(" to ".text(",
    )
    // 26.1: Fabric API renames
    oneWay(current.parsed >= "26.1",
        "\\bkeybinding\\.v1\\.KeyBindingHelper\\b" to "keymapping.v1.KeyMappingHelper",
        "\\bKeyBindingHelper\\.getBoundKeyOf\\(" to "KeyMappingHelper.getBoundKeyOf(",
    )
    // 26.2: the open screen moved from Minecraft to Gui, on-screen messages and "hide GUI" to Gui's Hud,
    // and the camera and main render target have shorter getters. "client" and "minecraft" are always
    // a Minecraft in this code.
    oneWay(current.parsed >= "26.2",
        "(?<![.\\w])(client|minecraft)\\.screen\\b" to "$1.gui.screen()",
        "(?<![.\\w])client\\.setScreen\\(" to "client.gui.setScreen(",
        "\\bclient\\.gui\\.setOverlayMessage\\(" to "client.gui.hud.setOverlayMessage(",
        "\\bclient\\.options\\.hideGui\\b" to "client.gui.hud.isHidden()",
        "\\.getMainCamera\\(\\)" to ".mainCamera()",
        "\\bclient\\.getMainRenderTarget\\(\\)" to "client.gameRenderer.mainRenderTarget()",
    )
    // 26.3: input goes through SDL. Keyboard keys are a key type of their own (SDL scancodes) and there is no
    // GLFW; InputConstants names key presses, releases and repeats on every version.
    oneWay(current.parsed >= "26.3",
        "\\bInputConstants\\.Type\\.KEYSYM\\b" to "InputConstants.Type.KEYBOARD",
        "(?<![.\\w])GLFW\\.GLFW_PRESS\\b" to "InputConstants.PRESS",
        // dev test only
        "\\borg\\.lwjgl\\.glfw\\.GLFW\\.GLFW_(PRESS|RELEASE|REPEAT)\\b" to "com.mojang.blaze3d.platform.InputConstants.$1",
    )
}
