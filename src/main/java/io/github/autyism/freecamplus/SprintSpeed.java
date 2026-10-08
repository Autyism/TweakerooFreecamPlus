package io.github.autyism.freecamplus;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
//? if >=26.3 {
/*import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.system.MemoryStack;
*///?} else
import org.lwjgl.glfw.GLFW;

/**
 * The free camera's sprint multiplier. Starts at Tweakeroo's own 3.0 every time the game starts;
 * the wheel steps through these values.
 */
public final class SprintSpeed {
	private static final double[] STEPS = {1, 1.5, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64};
	private static final int DEFAULT_INDEX = 3; // 3.0, Tweakeroo's value

	private static int index = DEFAULT_INDEX;

	private SprintSpeed() {
	}

	public static double factor() {
		return STEPS[index];
	}

	public static void step(int direction) {
		index = Math.max(0, Math.min(STEPS.length - 1, index + direction));
	}

	/** Set by the dev test, which cannot hold a real key. Null in normal play. */
	public static Boolean testSprintHeld;

	/**
	 * Whether the sprint key is physically held down right now. Not {@code sprintKey.isPressed()}:
	 * with "Toggle Sprint" on, that stays true after the key is let go.
	 */
	public static boolean sprintKeyHeld(Minecraft client) {
		if (testSprintHeld != null) {
			return testSprintHeld;
		}
		InputConstants.Key key = KeyBindingHelper.getBoundKeyOf(client.options.keySprint);
		//? if >=26.3 {
		/*// 26.3+ reads input through SDL: keyboard keys are SDL scancodes, mouse buttons count from 1.
		if (key.getType() == InputConstants.Type.MOUSE) {
			return isMouseButtonDown(key.getValue());
		}
		return key.getValue() != InputConstants.UNKNOWN.getValue() && InputConstants.isKeyDown(key.getValue());
		*///?} else {
		long window = client.getWindow().handle();
		if (key.getType() == InputConstants.Type.MOUSE) {
			return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
		}
		return key.getValue() != InputConstants.UNKNOWN.getValue() && GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
		//?}
	}

	//? if >=26.3 {
	/*/^* Whether a mouse button (numbered from 1, as SDL does) is held. ^/
	private static boolean isMouseButtonDown(int button) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			int buttons = SDLMouse.SDL_GetMouseState(stack.mallocFloat(1), stack.mallocFloat(1));
			return button > 0 && button <= 32 && (buttons & 1 << button - 1) != 0;
		}
	}
	*///?}

	/** For the test only. */
	public static void reset() {
		index = DEFAULT_INDEX;
	}
}
