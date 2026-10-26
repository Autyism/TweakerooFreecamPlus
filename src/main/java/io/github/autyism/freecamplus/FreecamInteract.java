package io.github.autyism.freecamplus;

//? if >=1.21.11 <26.1 {
import fi.dy.masa.tweakeroo.util.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
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
 * </ul>
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

	/** Before placing or using on a block: the character faces the camera's way (and sneaks if sneak is held) */
	public static void beginPlace(Minecraft mc) {
		if (!active(mc)) return;
		Entity camera = mc.getCameraEntity();
		face(mc, camera.getYRot(), camera.getXRot());
		sneak(mc);
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

	/** Every client tick: send the restored direction once the click went out */
	public static void tick(Minecraft mc) {
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

	private static void sneak(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (sneakSent || p.input == null || !mc.options.keyShift.isDown()) return;
		savedInput = p.input.keyPresses;
		Input s = savedInput;
		p.connection.send(new ServerboundPlayerInputPacket(new Input(s.forward(), s.backward(), s.left(), s.right(), s.jump(), true, s.sprint())));
		sneakSent = true;
	}
	//?}
}
