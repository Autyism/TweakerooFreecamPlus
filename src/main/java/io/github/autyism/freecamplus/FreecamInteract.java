package io.github.autyism.freecamplus;

//? if >=1.21.11 <26.1 {
import fi.dy.masa.tweakeroo.util.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
//?}

/**
 * Using things from Tweakeroo's free camera: left and right click work on what the camera looks at, as long as your
 * character could reach it (the server checks the reach from your character).
 * <ul>
 * <li>The crosshair target comes from the camera; your own character and anything out of your character's reach are
 * not targeted.</li>
 * <li>Placing a block turns your character to the camera's direction for that moment, so stairs, observers, pistons
 * and the like face the way the camera looks; afterwards your character turns back.</li>
 * <li>Using an item that aims by itself (a bucket, for example) turns your character towards the block the camera
 * looks at, so the server's aim lands on the same block.</li>
 * <li>Holding sneak (which moves the camera down) sneaks your character for that click: placing against chests and
 * other blocks you can open works like with sneak held.</li>
 * <li>Ctrl + L locks the placing direction to the one of your last placement (the camera's, if nothing was placed
 * yet in this free camera), so the camera can look around while blocks keep facing that way; pressed again, blocks
 * follow the camera again.</li>
 * <li>Ctrl + Right Shift locks sneak for clicks until pressed again: every click from the free camera is a sneaking
 * click, as if sneak were held. Your character itself never sneaks (it does not crouch, and a flying character does
 * not sink), and turning the lock on clicks nothing.</li>
 * </ul>
 * Both locks end when you leave the free camera.
 */
public final class FreecamInteract {
	private FreecamInteract() {
	}

	//? if >=1.21.11 <26.1 {
	private static boolean facing;
	private static int depth;
	private static float savedYaw, savedPitch, savedYRotO, savedXRotO, savedHead, savedBody;
	private static int restoreIn = -1;
	private static Input savedInput;
	private static boolean sneakSent;
	/** A click from the free camera is going on with sneak (the game's own click handling sees the character sneaking) */
	private static boolean clickSneak;
	/** Direction of the last placement from the free camera (for the direction lock) */
	private static boolean hasLast;
	private static float lastYaw, lastPitch;
	private static boolean directionLocked;
	private static float lockedYaw, lockedPitch;
	private static boolean sneakLocked;

	/** The free camera is on and the camera is not the character */
	public static boolean active(Minecraft mc) {
		Entity camera = CameraEntity.getCamera();
		return camera != null && mc.player != null && mc.getCameraEntity() == camera;
	}

	/** After the game picked its crosshair target: the camera's, limited to what the character can reach */
	public static void repick(Minecraft mc, float partialTick) {
		if (!active(mc) || mc.level == null) return;
		Entity camera = mc.getCameraEntity();
		HitResult hit = mc.player.raycastHitResult(partialTick, camera);
		if (hit instanceof EntityHitResult e && (e.getEntity() == mc.player || !mc.player.isWithinEntityInteractionRange(e.getEntity().getBoundingBox(), 1.0))) {
			hit = miss(camera, hit.getLocation());
		} else if (hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK && !mc.player.isWithinBlockInteractionRange(b.getBlockPos(), 1.0)) {
			hit = miss(camera, hit.getLocation());
		}
		mc.hitResult = hit;
		mc.crosshairPickEntity = hit instanceof EntityHitResult e ? e.getEntity() : null;
	}

	private static BlockHitResult miss(Entity camera, Vec3 at) {
		Vec3 look = camera.getViewVector(1.0f);
		return BlockHitResult.miss(at, net.minecraft.core.Direction.getApproximateNearest(look.x, look.y, look.z), BlockPos.containing(at));
	}

	/** Before placing or using on a block: the character faces the camera's way, or the locked one (and sneaks if sneak is held) */
	public static void beginPlace(Minecraft mc) {
		if (!active(mc)) return;
		if (directionLocked) {
			face(mc, lockedYaw, lockedPitch);
		} else {
			Entity camera = mc.getCameraEntity();
			face(mc, camera.getYRot(), camera.getXRot());
			hasLast = true;
			lastYaw = camera.getYRot();
			lastPitch = camera.getXRot();
		}
		sneak(mc);
	}

	/** Ctrl + L in the free camera */
	public static void toggleDirectionLock(Minecraft mc) {
		if (directionLocked) {
			directionLocked = false;
			mc.gui.setOverlayMessage(Component.translatable("freecamplus.lock.direction.off"), false);
			return;
		}
		Entity camera = mc.getCameraEntity();
		lockedYaw = hasLast ? lastYaw : camera.getYRot();
		lockedPitch = hasLast ? lastPitch : camera.getXRot();
		directionLocked = true;
		mc.gui.setOverlayMessage(Component.translatable("freecamplus.lock.direction.on", directionText()), false);
	}

	/** Ctrl + Right Shift in the free camera */
	public static void toggleSneakLock(Minecraft mc) {
		sneakLocked = !sneakLocked;
		mc.gui.setOverlayMessage(Component.translatable(sneakLocked ? "freecamplus.lock.sneak.on" : "freecamplus.lock.sneak.off"), false);
	}

	public static boolean directionLocked() {
		return directionLocked;
	}

	public static boolean sneakLocked() {
		return sneakLocked;
	}

	/** The locked direction in words: the way blocks such as stairs face, and up / down for pistons and observers */
	public static Component directionText() {
		Component horizontal = Component.translatable("freecamplus.direction." + Direction.fromYRot(lockedYaw).getSerializedName());
		if (lockedPitch > 45) return Component.translatable("freecamplus.direction.withVertical", horizontal, Component.translatable("freecamplus.direction.down"));
		if (lockedPitch < -45) return Component.translatable("freecamplus.direction.withVertical", horizontal, Component.translatable("freecamplus.direction.up"));
		return horizontal;
	}

	/** Your character counts as sneaking for the game's own handling of a sneaking click (only during that click) */
	public static boolean sneakingFor(LocalPlayer player) {
		return clickSneak && player == Minecraft.getInstance().player;
	}

	/** Before using an item that aims by itself: the character looks at the block the camera looks at */
	public static void beginUse(Minecraft mc) {
		if (!active(mc)) return;
		LocalPlayer p = mc.player;
		if (mc.hitResult instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
			Vec3 eye = p.getEyePosition();
			Vec3 target = b.getLocation().subtract(Vec3.atLowerCornerOf(b.getDirection().getUnitVec3i()).scale(0.01));
			Vec3 d = target.subtract(eye);
			double flat = Math.sqrt(d.x * d.x + d.z * d.z);
			float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
			float pitch = (float) -Math.toDegrees(Math.atan2(d.y, flat));
			face(mc, yaw, pitch);
		} else {
			Entity camera = mc.getCameraEntity();
			face(mc, camera.getYRot(), camera.getXRot());
		}
		sneak(mc);
	}

	public static void end(Minecraft mc) {
		if (sneakSent) {
			sneakSent = false;
			clickSneak = false;
			if (mc.player != null && savedInput != null) mc.player.connection.send(new ServerboundPlayerInputPacket(savedInput));
			savedInput = null;
		}
		if (!facing) return;
		if (--depth > 0) return;
		facing = false;
		LocalPlayer p = mc.player;
		if (p == null) return;
		p.setYRot(savedYaw);
		p.setXRot(savedPitch);
		p.yRotO = savedYRotO;
		p.xRotO = savedXRotO;
		p.setYHeadRot(savedHead);
		p.setYBodyRot(savedBody);
		// the server learns the old direction on the next tick (in the same tick it would apply to this very click)
		restoreIn = 1;
	}

	/** Every client tick: send the restored direction once the click went out; the locks end with the free camera */
	public static void tick(Minecraft mc) {
		if (!active(mc) && (directionLocked || sneakLocked || hasLast)) {
			directionLocked = false;
			sneakLocked = false;
			hasLast = false;
		}
		if (restoreIn < 0) return;
		if (restoreIn-- > 0) return;
		restoreIn = -1;
		LocalPlayer p = mc.player;
		if (p != null && !facing) p.connection.send(new ServerboundMovePlayerPacket.Rot(p.getYRot(), p.getXRot(), p.onGround(), p.horizontalCollision));
	}

	private static void face(Minecraft mc, float yaw, float pitch) {
		LocalPlayer p = mc.player;
		if (facing) {
			depth++;
			return;
		}
		savedYaw = p.getYRot();
		savedPitch = p.getXRot();
		savedYRotO = p.yRotO;
		savedXRotO = p.xRotO;
		savedHead = p.getYHeadRot();
		savedBody = p.yBodyRot;
		restoreIn = -1;
		p.setYRot(yaw);
		p.setXRot(pitch);
		p.setYHeadRot(yaw);
		p.setYBodyRot(yaw);
		p.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround(), p.horizontalCollision));
		facing = true;
		depth = 1;
	}

	/**
	 * Sneak held or the sneak lock on: the server sees the character sneaking for this one click only (the input
	 * before and after it, all within the same tick of the server, so the character never crouches or sinks).
	 */
	private static void sneak(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (sneakSent || !(sneakLocked || mc.options.keyShift.isDown())) return;
		// in the free camera Tweakeroo's still-standing input is what the server last got
		savedInput = Input.EMPTY;
		p.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
		sneakSent = true;
		clickSneak = true;
	}
	//?}
}
