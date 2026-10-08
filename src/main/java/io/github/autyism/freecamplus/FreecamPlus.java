package io.github.autyism.freecamplus;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyism.freecamplus.mixin.CameraEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
*///?} else
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add-on for Tweakeroo's Free Camera (no switches, always on):
 * <ul>
 * <li>middle click while in the free camera sets a waypoint on the block you look at, or removes
 * the waypoint you look at. Waypoints work like QoL Bundle's Freecam markers: a box and beam in
 * the world, arrows around the crosshair, removed when you walk up to them.</li>
 * <li>sprint key (Ctrl) + mouse wheel in the free camera changes the sprint speed
 * ({@link SprintSpeed}); a line at the top shows whether sprint is on and the current factor.</li>
 * </ul>
 */
public class FreecamPlus implements ClientModInitializer {
	public static final String MOD_ID = "tweakeroo_freecam_plus";
	public static final Logger LOGGER = LoggerFactory.getLogger("FreecamPlus");

	private static final double MARK_REACH = 256.0;
	/** A waypoint is "looked at" when it is within this many degrees of the view direction. */
	private static final double PICK_DEGREES = 6.0;
	private static final int RING = 62;
	private static final int COLOR = 0xFFFF55FF;
	private static final int SELF_COLOR = 0xFF55FFFF;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_CLIENT_TICK.register(FreecamPlus::onStartTick);
		ClientTickEvents.END_CLIENT_TICK.register(FreecamPlus::onEndTick);
		//? if >=26.1 {
		/*LevelRenderEvents.BEFORE_GIZMOS.register(context -> renderWorld());
		*///?} else
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> renderWorld());
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID, "hud"), FreecamPlus::renderHud);
	}

	/** Before the game handles its key presses: middle clicks in the free camera are ours. */
	private static void onStartTick(Minecraft client) {
		if (CameraEntity.getCamera() == null || client.level == null) {
			return;
		}
		boolean clicked = false;
		while (client.options.keyPickItem.consumeClick()) {
			clicked = true;
		}
		client.options.keyPickItem.setDown(false);
		if (clicked && client.screen == null) {
			toggleMarkerAtCrosshair(client);
		}
	}

	private static void onEndTick(Minecraft client) {
		Markers.sync(client);
		if (client.player != null && client.level != null && !Markers.list().isEmpty()) {
			Markers.removeArrived(dimension(client), client.player.blockPosition());
		}
	}

	private static String dimension(Minecraft client) {
		return client.level.dimension().identifier().toString();
	}

	/** Removes the waypoint closest to the middle of the view; with none there, sets one. */
	public static void toggleMarkerAtCrosshair(Minecraft client) {
		Camera camera = client.gameRenderer.getMainCamera();
		Vec3 eye = camera.position();
		Vec3 look = Vec3.directionFromRotation(camera.xRot(), camera.yRot());
		String dimension = dimension(client);

		BlockHitResult hit = client.level.clip(new ClipContext(eye, eye.add(look.scale(MARK_REACH)),
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, client.player));
		BlockPos where = hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : BlockPos.containing(eye);

		Markers.Marker best = null;
		double bestAngle = PICK_DEGREES;
		for (Markers.Marker marker : Markers.list()) {
			if (!marker.dimension().equals(dimension)) {
				continue;
			}
			if (marker.pos().equals(where)) {
				best = marker; // the spot a new waypoint would go already has one
				break;
			}
			Vec3 to = Vec3.atCenterOf(marker.pos()).subtract(eye);
			if (to.lengthSqr() < 1.0E-4) {
				continue;
			}
			double angle = Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(look), -1.0, 1.0)));
			if (angle < bestAngle) {
				bestAngle = angle;
				best = marker;
			}
		}
		if (best != null) {
			Markers.remove(best);
			client.gui.setOverlayMessage(Component.translatable("freecamplus.marker.removed", best.number()), false);
			return;
		}

		Markers.Marker marker = Markers.add(dimension, where);
		client.gui.setOverlayMessage(Component.translatable("freecamplus.marker.added", marker.number(), where.getX(), where.getY(), where.getZ()), false);
	}

	private static void renderWorld() {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}
		if (CameraEntity.getCamera() != null && SelfPointer.visible() && client.player != null) {
			// your own body, outlined through walls
			Gizmos.cuboid(client.player.getBoundingBox().inflate(0.05), GizmoStyle.stroke(SELF_COLOR, 3.0F)).setAlwaysOnTop();
		}
		String here = dimension(client);
		for (Markers.Marker marker : Markers.list()) {
			if (!marker.dimension().equals(here)) {
				continue;
			}
			Gizmos.cuboid(new AABB(marker.pos()).inflate(0.03), GizmoStyle.stroke(COLOR, 3.0F)).setAlwaysOnTop();
			Vec3 base = Vec3.atCenterOf(marker.pos());
			Gizmos.line(base, base.add(0, 24, 0), 0xB0FF55FF, 3.0F).setAlwaysOnTop();
		}
	}

	private static void renderHud(GuiGraphics context, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || client.options.hideGui) {
			return;
		}
		if (!Markers.list().isEmpty()) {
			drawMarkerArrows(context, client);
		}
		if (CameraEntity.getCamera() != null && SelfPointer.visible()) {
			Vec3 body = client.player.getEyePosition();
			int distance = (int) Math.round(body.distanceTo(client.gameRenderer.getMainCamera().position()));
			drawArrow(context, client, body, SELF_COLOR, Component.translatable("freecamplus.self", distance));
		}
		if (CameraEntity.getCamera() != null) {
			boolean on = CameraEntityAccessor.freecamplus$isSprinting();
			String factor = SprintSpeed.factor() == Math.floor(SprintSpeed.factor())
					? String.valueOf((int) SprintSpeed.factor()) : String.valueOf(SprintSpeed.factor());
			Component text = Component.translatable(on ? "freecamplus.sprint.on" : "freecamplus.sprint.off", factor);
			int width = client.font.width(text);
			int x = (context.guiWidth() - width) / 2;
			context.drawString(client.font, text, x, 4, on ? 0xFF55FF55 : 0xFFAAAAAA);
		}
	}

	/** One arrow per waypoint on a ring around the crosshair, with its number and distance. */
	private static void drawMarkerArrows(GuiGraphics context, Minecraft client) {
		String here = dimension(client);
		Vec3 eye = client.gameRenderer.getMainCamera().position();
		for (Markers.Marker marker : Markers.list()) {
			if (!marker.dimension().equals(here)) {
				continue;
			}
			Vec3 spot = Vec3.atCenterOf(marker.pos());
			int distance = (int) Math.round(spot.distanceTo(eye));
			double height = spot.y - eye.y;
			drawArrow(context, client, spot, COLOR, Component.translatable("freecamplus.marker.label", marker.number(), distance)
					.append(height > 3 ? Component.translatable("freecamplus.marker.above")
							: height < -3 ? Component.translatable("freecamplus.marker.below") : Component.empty()));
		}
	}

	/** An arrow on the ring around the crosshair, turned towards {@code spot}, with a label. */
	private static void drawArrow(GuiGraphics context, Minecraft client, Vec3 spot, int color, Component label) {
		Camera camera = client.gameRenderer.getMainCamera();
		Vec3 eye = camera.position();
		int screenWidth = context.guiWidth();
		int centerX = screenWidth / 2;
		int centerY = context.guiHeight() / 2;
		float angle = (float) Math.toRadians(relativeAngle(eye, camera.yRot(), spot));
		context.pose().pushMatrix();
		context.pose().translate(centerX, centerY);
		context.pose().rotate(angle);
		for (int row = 0; row < 6; row++) {
			context.fill(-row, -RING + row, row + 1, -RING + row + 1, color);
		}
		context.pose().popMatrix();

		int width = client.font.width(label);
		int labelRadius = RING + 12;
		int x = centerX + Math.round(Mth.sin(angle) * (labelRadius + width / 2F)) - width / 2;
		int y = centerY - Math.round(Mth.cos(angle) * labelRadius) - 4;
		x = Mth.clamp(x, 2, screenWidth - width - 2);
		context.fill(x - 2, y - 1, x + width + 2, y + 9, 0x80000000);
		context.drawString(client.font, label, x, y, color);
	}

	/** Degrees to turn from the camera's yaw to face {@code to}; same as QoL Bundle's Sound Compass. */
	private static float relativeAngle(Vec3 from, float cameraYaw, Vec3 to) {
		// Minecraft yaw: 0 = south (+Z), 90 = west (-X), growing clockwise seen from above.
		float yawToTarget = (float) Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
		return Mth.wrapDegrees(yawToTarget - cameraYaw);
	}
}
