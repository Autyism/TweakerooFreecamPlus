package io.github.autyism.freecamplus;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
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
	public static boolean sprintKeyHeld(MinecraftClient client) {
		if (testSprintHeld != null) {
			return testSprintHeld;
		}
		InputUtil.Key key = KeyBindingHelper.getBoundKeyOf(client.options.sprintKey);
		long window = client.getWindow().getHandle();
		if (key.getCategory() == InputUtil.Type.MOUSE) {
			return GLFW.glfwGetMouseButton(window, key.getCode()) == GLFW.GLFW_PRESS;
		}
		return key.getCode() != InputUtil.UNKNOWN_KEY.getCode() && GLFW.glfwGetKey(window, key.getCode()) == GLFW.GLFW_PRESS;
	}

	/** For the test only. */
	public static void reset() {
		index = DEFAULT_INDEX;
	}
}
