package io.github.autyism.freecamplus;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyism.freecamplus.mixin.CameraEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
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
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> renderWorld());
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(MOD_ID, "hud"), FreecamPlus::renderHud);
	}

	/** Before the game handles its key presses: middle clicks in the free camera are ours. */
	private static void onStartTick(MinecraftClient client) {
		if (CameraEntity.getCamera() == null || client.world == null) {
			return;
		}
		boolean clicked = false;
		while (client.options.pickItemKey.wasPressed()) {
			clicked = true;
		}
		client.options.pickItemKey.setPressed(false);
		if (clicked && client.currentScreen == null) {
			toggleMarkerAtCrosshair(client);
		}
	}

	private static void onEndTick(MinecraftClient client) {
		Markers.sync(client);
		if (client.player != null && client.world != null && !Markers.list().isEmpty()) {
			Markers.removeArrived(dimension(client), client.player.getBlockPos());
		}
	}

	private static String dimension(MinecraftClient client) {
		return client.world.getRegistryKey().getValue().toString();
	}

	/** Removes the waypoint closest to the middle of the view; with none there, sets one. */
	public static void toggleMarkerAtCrosshair(MinecraftClient client) {
		Camera camera = client.gameRenderer.getCamera();
		Vec3d eye = camera.getCameraPos();
		Vec3d look = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
		String dimension = dimension(client);

		BlockHitResult hit = client.world.raycast(new RaycastContext(eye, eye.add(look.multiply(MARK_REACH)),
				RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player));
		BlockPos where = hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : BlockPos.ofFloored(eye);

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
			Vec3d to = Vec3d.ofCenter(marker.pos()).subtract(eye);
			if (to.lengthSquared() < 1.0E-4) {
				continue;
			}
			double angle = Math.toDegrees(Math.acos(MathHelper.clamp(to.normalize().dotProduct(look), -1.0, 1.0)));
			if (angle < bestAngle) {
				bestAngle = angle;
				best = marker;
			}
		}
		if (best != null) {
			Markers.remove(best);
			client.inGameHud.setOverlayMessage(Text.translatable("freecamplus.marker.removed", best.number()), false);
			return;
		}

		Markers.Marker marker = Markers.add(dimension, where);
		client.inGameHud.setOverlayMessage(Text.translatable("freecamplus.marker.added", marker.number(), where.getX(), where.getY(), where.getZ()), false);
	}

	private static void renderWorld() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			return;
		}
		if (CameraEntity.getCamera() != null && SelfPointer.visible() && client.player != null) {
			// your own body, outlined through walls
			GizmoDrawing.box(client.player.getBoundingBox().expand(0.05), DrawStyle.stroked(SELF_COLOR, 3.0F)).ignoreOcclusion();
		}
		String here = dimension(client);
		for (Markers.Marker marker : Markers.list()) {
			if (!marker.dimension().equals(here)) {
				continue;
			}
			GizmoDrawing.box(new Box(marker.pos()).expand(0.03), DrawStyle.stroked(COLOR, 3.0F)).ignoreOcclusion();
			Vec3d base = Vec3d.ofCenter(marker.pos());
			GizmoDrawing.line(base, base.add(0, 24, 0), 0xB0FF55FF, 3.0F).ignoreOcclusion();
		}
	}

	private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null || client.options.hudHidden) {
			return;
		}
		if (!Markers.list().isEmpty()) {
			drawMarkerArrows(context, client);
		}
		if (CameraEntity.getCamera() != null && SelfPointer.visible()) {
			Vec3d body = client.player.getEyePos();
			int distance = (int) Math.round(body.distanceTo(client.gameRenderer.getCamera().getCameraPos()));
			drawArrow(context, client, body, SELF_COLOR, Text.translatable("freecamplus.self", distance));
		}
		if (CameraEntity.getCamera() != null) {
			boolean on = CameraEntityAccessor.freecamplus$isSprinting();
			String factor = SprintSpeed.factor() == Math.floor(SprintSpeed.factor())
					? String.valueOf((int) SprintSpeed.factor()) : String.valueOf(SprintSpeed.factor());
			Text text = Text.translatable(on ? "freecamplus.sprint.on" : "freecamplus.sprint.off", factor);
			int width = client.textRenderer.getWidth(text);
			int x = (context.getScaledWindowWidth() - width) / 2;
			context.drawTextWithShadow(client.textRenderer, text, x, 4, on ? 0xFF55FF55 : 0xFFAAAAAA);
		}
	}

	/** One arrow per waypoint on a ring around the crosshair, with its number and distance. */
	private static void drawMarkerArrows(DrawContext context, MinecraftClient client) {
		String here = dimension(client);
		Vec3d eye = client.gameRenderer.getCamera().getCameraPos();
		for (Markers.Marker marker : Markers.list()) {
			if (!marker.dimension().equals(here)) {
				continue;
			}
			Vec3d spot = Vec3d.ofCenter(marker.pos());
			int distance = (int) Math.round(spot.distanceTo(eye));
			double height = spot.y - eye.y;
			drawArrow(context, client, spot, COLOR, Text.translatable("freecamplus.marker.label", marker.number(), distance)
					.append(height > 3 ? Text.translatable("freecamplus.marker.above")
							: height < -3 ? Text.translatable("freecamplus.marker.below") : Text.empty()));
		}
	}

	/** An arrow on the ring around the crosshair, turned towards {@code spot}, with a label. */
	private static void drawArrow(DrawContext context, MinecraftClient client, Vec3d spot, int color, Text label) {
		Camera camera = client.gameRenderer.getCamera();
		Vec3d eye = camera.getCameraPos();
		int screenWidth = context.getScaledWindowWidth();
		int centerX = screenWidth / 2;
		int centerY = context.getScaledWindowHeight() / 2;
		float angle = (float) Math.toRadians(relativeAngle(eye, camera.getYaw(), spot));
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(centerX, centerY);
		context.getMatrices().rotate(angle);
		for (int row = 0; row < 6; row++) {
			context.fill(-row, -RING + row, row + 1, -RING + row + 1, color);
		}
		context.getMatrices().popMatrix();

		int width = client.textRenderer.getWidth(label);
		int labelRadius = RING + 12;
		int x = centerX + Math.round(MathHelper.sin(angle) * (labelRadius + width / 2F)) - width / 2;
		int y = centerY - Math.round(MathHelper.cos(angle) * labelRadius) - 4;
		x = MathHelper.clamp(x, 2, screenWidth - width - 2);
		context.fill(x - 2, y - 1, x + width + 2, y + 9, 0x80000000);
		context.drawTextWithShadow(client.textRenderer, label, x, y, color);
	}

	/** Degrees to turn from the camera's yaw to face {@code to}; same as QoL Bundle's Sound Compass. */
	private static float relativeAngle(Vec3d from, float cameraYaw, Vec3d to) {
		// Minecraft yaw: 0 = south (+Z), 90 = west (-X), growing clockwise seen from above.
		float yawToTarget = (float) Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
		return MathHelper.wrapDegrees(yawToTarget - cameraYaw);
	}
}
