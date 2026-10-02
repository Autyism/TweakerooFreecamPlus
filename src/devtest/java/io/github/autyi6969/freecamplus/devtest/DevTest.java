package io.github.autyi6969.freecamplus.devtest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyi6969.freecamplus.Markers;
import io.github.autyi6969.freecamplus.SprintSpeed;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dev-only automated test (never in the release jar). Logs lines starting with [DevTest]. */
public class DevTest implements ClientModInitializer {
	private static final Logger LOG = LoggerFactory.getLogger("DevTest");

	private record Step(String name, BiPredicate<MinecraftClient, Integer> action) {
	}

	private final List<Step> steps = new ArrayList<>();
	private int index;
	private int ticks;
	private int failures;
	private Vec3d before = Vec3d.ZERO;
	private double speedDefault;
	private int slotBefore;
	private int markersBefore;

	@Override
	public void onInitializeClient() {
		build();
		new java.io.File(MinecraftClient.getInstance().runDirectory, "screenshots/devtest").mkdirs();
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(MinecraftClient client) {
		if (index >= steps.size()) {
			return;
		}
		Step step = steps.get(index);
		boolean done;
		try {
			done = step.action.test(client, ticks);
		} catch (Throwable t) {
			LOG.error("[DevTest] ERROR in step {}", step.name, t);
			failures++;
			done = true;
		}
		ticks++;
		if (done) {
			index++;
			ticks = 0;
		}
	}

	private void run(String name, Consumer<MinecraftClient> action) {
		steps.add(new Step(name, (client, t) -> {
			action.accept(client);
			return true;
		}));
	}

	private void waitTicks(int count) {
		steps.add(new Step("wait " + count, (client, t) -> t >= count));
	}

	private void check(String name, BiPredicate<MinecraftClient, Integer> condition) {
		run(name, client -> {
			boolean ok = condition.test(client, 0);
			if (!ok) {
				failures++;
			}
			LOG.info("[DevTest] {} {}", ok ? "PASS" : "FAIL", name);
		});
	}

	private void screenshot(String name) {
		steps.add(new Step("screenshot " + name, (client, t) -> {
			if (t < 6) {
				return false;
			}
			ScreenshotRecorder.saveScreenshot(client.runDirectory, "devtest/" + name + ".png", client.getFramebuffer(), 1,
					message -> LOG.info("[DevTest] screenshot {}", name));
			return true;
		}));
	}

	private static void scroll(MinecraftClient client, double amount) {
		try {
			Method method = Mouse.class.getDeclaredMethod(net.fabricmc.loader.api.FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_312", "method_1598", "(JDD)V"), long.class, double.class, double.class);
			method.setAccessible(true);
			method.invoke(client.mouse, client.getWindow().getHandle(), 0.0, amount);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/** A real keyboard event for the sprint key, through the game's own key handler. */
	private static void sprintKeyEvent(MinecraftClient client, int action) {
		try {
			var resolver = net.fabricmc.loader.api.FabricLoader.getInstance().getMappingResolver();
			Method method = net.minecraft.client.Keyboard.class.getDeclaredMethod(
					resolver.mapMethodName("intermediary", "net.minecraft.class_309", "method_1466", "(JILnet/minecraft/class_11908;)V"),
					long.class, int.class, net.minecraft.client.input.KeyInput.class);
			method.setAccessible(true);
			int code = KeyBindingHelper.getBoundKeyOf(client.options.sprintKey).getCode();
			method.invoke(client.keyboard, client.getWindow().getHandle(), action, new net.minecraft.client.input.KeyInput(code, 0, 0));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean bodyToggleBefore;
	private boolean flickered;

	/** Toggle Sprint on, Ctrl held for 15 ticks with key-repeats: sprint must stay ON every tick. */
	private void flickerSteps() {
		run("toggle sprint on, remember the body's toggle", client -> {
			client.options.getSprintToggled().setValue(true);
			bodyToggleBefore = client.options.sprintKey.isPressed();
			flickered = false;
			SprintSpeed.testSprintHeld = true;
			sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_PRESS);
		});
		steps.add(new Step("hold Ctrl with key-repeats", (client, t) -> {
			sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_REPEAT);
			if (t > 1 && !io.github.autyi6969.freecamplus.mixin.CameraEntityAccessor.freecamplus$isSprinting()) {
				flickered = true;
			}
			return t >= 15;
		}));
		check("sprint stays ON while Ctrl is held (no flicker)", (client, t) -> !flickered);
		run("let go of Ctrl", client -> {
			sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_RELEASE);
			SprintSpeed.testSprintHeld = false;
		});
		waitTicks(3);
		check("sprint OFF after letting go (not moving)", (client, t) ->
				!io.github.autyi6969.freecamplus.mixin.CameraEntityAccessor.freecamplus$isSprinting());
		check("body's toggle sprint unchanged by Ctrl in freecam", (client, t) -> client.options.sprintKey.isPressed() == bodyToggleBefore);
	}

	/** Two taps must not show the pointer, three quick taps must. */
	private void selfPointerSteps() {
		run("tap Ctrl twice", client -> {
			for (int i = 0; i < 2; i++) {
				sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_PRESS);
				sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_RELEASE);
			}
		});
		check("two taps: no pointer", (client, t) -> !io.github.autyi6969.freecamplus.SelfPointer.visible());
		waitTicks(20); // longer than the tap window
		run("tap Ctrl three times", client -> {
			for (int i = 0; i < 3; i++) {
				sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_PRESS);
				sprintKeyEvent(client, org.lwjgl.glfw.GLFW.GLFW_RELEASE);
			}
		});
		check("three taps: pointer to yourself shown", (client, t) -> io.github.autyi6969.freecamplus.SelfPointer.visible());
		screenshot("06_self_pointer");
	}

	private static Vec3d cam() {
		return CameraEntity.getCamera().getEntityPos();
	}

	private static void middleClick(MinecraftClient client) {
		var camera = client.gameRenderer.getCamera();
		LOG.info("[DevTest] INFO middle click: camera at {} yaw {} pitch {}, waypoints before: {}", camera.getCameraPos(), camera.getYaw(), camera.getPitch(), Markers.list());
		KeyBinding.onKeyPressed(KeyBindingHelper.getBoundKeyOf(client.options.pickItemKey));
	}

	private void build() {
		steps.add(new Step("wait for the world", (client, t) -> client.world != null && client.player != null && t > 100));
		run("clear old waypoints", client -> new ArrayList<>(Markers.list()).forEach(Markers::remove));
		run("free camera on", client -> FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(true));
		waitTicks(5);
		check("free camera exists", (client, t) -> CameraEntity.getCamera() != null);
		check("default sprint factor is 3", (client, t) -> SprintSpeed.factor() == 3.0);
		screenshot("01_freecam_sprint_off");

		// default sprint speed
		run("hold forward + sprint", client -> {
			client.options.forwardKey.setPressed(true);
			client.options.sprintKey.setPressed(true);
			SprintSpeed.testSprintHeld = true;
		});
		waitTicks(30);
		run("measure start", client -> before = cam());
		waitTicks(10);
		run("measure end (default)", client -> {
			speedDefault = cam().subtract(before).horizontalLength() / 10;
			LOG.info("[DevTest] INFO sprint speed at x3: {} blocks/tick", speedDefault);
		});
		screenshot("02_freecam_sprint_on_x3");

		// Ctrl + wheel: 3 -> 4 -> 6, hotbar must not move
		run("scroll up twice with sprint held", client -> {
			slotBefore = client.player.getInventory().getSelectedSlot();
			scroll(client, 1.0);
			scroll(client, 1.0);
		});
		check("factor is now 6", (client, t) -> SprintSpeed.factor() == 6.0);
		check("hotbar slot did not change", (client, t) -> client.player.getInventory().getSelectedSlot() == slotBefore);
		waitTicks(30);
		run("measure start", client -> before = cam());
		waitTicks(10);
		check("sprint speed doubled (x6 vs x3)", (client, t) -> {
			double speed = cam().subtract(before).horizontalLength() / 10;
			LOG.info("[DevTest] INFO sprint speed at x6: {} blocks/tick, ratio {}", speed, speed / speedDefault);
			return Math.abs(speed / speedDefault - 2.0) < 0.1;
		});
		screenshot("03_freecam_sprint_on_x6");

		// without sprint held the wheel is the hotbar again
		run("release keys", client -> {
			client.options.forwardKey.setPressed(false);
			client.options.sprintKey.setPressed(false);
			SprintSpeed.testSprintHeld = false;
		});
		waitTicks(10);
		run("sprint toggled on but Ctrl not held: scroll", client -> {
			client.options.sprintKey.setPressed(true); // what Toggle Sprint leaves behind
			scroll(client, 1.0);
			client.options.sprintKey.setPressed(false);
		});
		check("toggled sprint without Ctrl held does not change speed", (client, t) -> SprintSpeed.factor() == 6.0);
		run("scroll without sprint", client -> {
			slotBefore = client.player.getInventory().getSelectedSlot();
			scroll(client, -1.0);
		});
		check("factor unchanged without sprint", (client, t) -> SprintSpeed.factor() == 6.0);
		check("hotbar slot changed without sprint", (client, t) -> client.player.getInventory().getSelectedSlot() != slotBefore);
		run("scroll back", client -> scroll(client, 1.0));
		run("sprint down to the bottom and back", client -> {
			SprintSpeed.testSprintHeld = true;
			for (int i = 0; i < 30; i++) {
				scroll(client, -1.0);
			}
		});
		check("factor stops at 1", (client, t) -> SprintSpeed.factor() == 1.0);
		run("back to 3", client -> {
			for (int i = 0; i < 3; i++) {
				scroll(client, 1.0);
			}
			SprintSpeed.testSprintHeld = false;
		});
		check("factor back at 3", (client, t) -> SprintSpeed.factor() == 3.0);

		// waypoints with the middle button
		run("look down at the ground", client -> {
			CameraEntity camera = CameraEntity.getCamera();
			// back over loaded ground, 20 blocks in front of and 12 above the body
			camera.setPos(client.player.getX(), client.player.getY() + 12, client.player.getZ() + 20);
			camera.setCameraRotations(0F, 50F);
			slotBefore = client.player.getInventory().getSelectedSlot();
		});
		waitTicks(3);
		run("middle click", DevTest::middleClick);
		waitTicks(3);
		check("one waypoint set", (client, t) -> Markers.list().size() == 1);
		check("the body did not pick a block", (client, t) -> client.player.getInventory().getSelectedSlot() == slotBefore);
		screenshot("04_waypoint_set");
		run("middle click on it again", DevTest::middleClick);
		waitTicks(3);
		check("waypoint removed", (client, t) -> Markers.list().isEmpty());
		run("middle click to set it again", DevTest::middleClick);
		waitTicks(3);
		check("waypoint set again", (client, t) -> Markers.list().size() == 1);
		run("turn a little and set a second one", client -> {
			CameraEntity camera = CameraEntity.getCamera();
			camera.setCameraRotations(camera.getYaw() + 40F, 35F);
		});
		waitTicks(3);
		run("middle click", DevTest::middleClick);
		waitTicks(3);
		check("two waypoints", (client, t) -> Markers.list().size() == 2);

		selfPointerSteps();
		flickerSteps();

		run("speed up to x6, then switch freecam off and on", client -> {
			SprintSpeed.testSprintHeld = true;
			scroll(client, 1.0);
			scroll(client, 1.0);
			SprintSpeed.testSprintHeld = false;
			LOG.info("[DevTest] INFO factor before re-toggle: {}", SprintSpeed.factor());
			FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);
		});
		waitTicks(3);
		run("freecam on again", client -> FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(true));
		waitTicks(3);
		check("re-toggled freecam is back at x3", (client, t) -> SprintSpeed.factor() == 3.0);

		run("free camera off", client -> FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false));
		waitTicks(10);
		check("free camera gone", (client, t) -> CameraEntity.getCamera() == null);
		check("waypoints still there", (client, t) -> Markers.list().size() == 2);
		screenshot("05_body_view_arrows");
		run("middle click outside freecam", client -> {
			markersBefore = Markers.list().size();
			middleClick(client);
		});
		waitTicks(3);
		check("outside freecam middle click does not touch waypoints", (client, t) -> Markers.list().size() == markersBefore);
		run("clear test waypoints", client -> new ArrayList<>(Markers.list()).forEach(Markers::remove));

		if (System.getProperty("fcp.xaero") != null) {
			xaeroSteps();
		}

		run("summary and stop", client -> {
			LOG.info("[DevTest] SUMMARY failures={}", failures);
			LOG.info("[DevTest] DONE");
			client.scheduleStop();
		});
	}

	@org.jspecify.annotations.Nullable
	private static KeyBinding xaeroMapKey(MinecraftClient client) {
		for (KeyBinding key : client.options.allKeys) {
			if (key.getId().equals("gui.xaero_open_map")) {
				return key;
			}
		}
		return null;
	}

	private void openMap(String name) {
		run("open xaero world map (" + name + ")", client -> {
			KeyBinding key = xaeroMapKey(client);
			LOG.info("[DevTest] INFO world map key {} bound to {}", key == null ? null : key.getId(),
					key == null ? null : key.getBoundKeyTranslationKey());
			KeyBinding.onKeyPressed(KeyBindingHelper.getBoundKeyOf(key));
		});
		steps.add(new Step("wait for the map screen", (client, t) -> client.currentScreen != null || t > 60));
		waitTicks(40); // opening animation
		run("log " + name, client -> LOG.info("[DevTest] INFO {}: screen {} player at {} yaw {}", name,
				client.currentScreen == null ? null : client.currentScreen.getClass().getName(),
				client.player.getBlockPos(), client.player.getYaw()));
		screenshot(name);
		run("close map", client -> client.setScreen(null));
		waitTicks(10);
	}

	/** Xaero's world map: face east, look at the map, move 40 blocks east, look again. */
	private void xaeroSteps() {
		run("face east", client -> {
			client.player.setYaw(-90F);
			client.player.setPitch(0F);
		});
		run("list key bindings", client -> {
			for (KeyBinding key : client.options.allKeys) {
				if (key.getId().toLowerCase().contains("xaero")) {
					LOG.info("[DevTest] INFO key {} = {}", key.getId(), key.getBoundKeyTranslationKey());
				}
			}
		});
		waitTicks(20);
		openMap("10_map_facing_east_before");
		run("walk east (forward)", client -> {
			client.player.setYaw(-90F);
			client.options.forwardKey.setPressed(true);
			client.options.sprintKey.setPressed(true);
		});
		waitTicks(60);
		run("stop walking", client -> {
			client.options.forwardKey.setPressed(false);
			client.options.sprintKey.setPressed(false);
		});
		waitTicks(10);
		openMap("11_map_after_walking_east");
		run("teleport 64 blocks east", client -> client.player.networkHandler.sendChatCommand("tp @s ~64 ~ ~"));
		waitTicks(40);
		openMap("12_map_after_tp_64_east");
		run("teleport 64 blocks north", client -> client.player.networkHandler.sendChatCommand("tp @s ~ ~ ~-64"));
		waitTicks(40);
		openMap("13_map_after_tp_64_north");
	}
}
