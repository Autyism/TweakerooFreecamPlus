package io.github.autyism.freecamplus;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * While the free camera is on, your character stays where it was. When it gets hurt you hear a bell and see a red
 * line with its health, so you can go back in time. (1.21.11: the setting "Leave When Hurt" also switches the free
 * camera off.)
 */
public final class BodyWatch {
	private static int lastHurtTime;
	private static long lastAlert;
	/** For the dev test: how many alerts were shown. */
	public static int alerts;
	/** A hit was noticed: the alert goes out on the next tick, when the new health has arrived too. */
	private static boolean pending;

	private BodyWatch() {
	}

	static void tick(Minecraft client) {
		if (client.player == null) {
			lastHurtTime = 0;
			return;
		}
		tickTemporary();
		if (pending) {
			pending = false;
			alert(client);
		}
		int hurt = client.player.hurtTime;
		boolean newHit = hurt > lastHurtTime;
		lastHurtTime = hurt;
		if (newHit && CameraEntity.getCamera() != null) {
			pending = true;
		}
	}

	/** Waypoint left where the camera was when the free camera was switched off because of a hit. */
	private static Markers.Marker temporary;
	/** The free camera was seen off since the temporary waypoint was set (so switching it on again removes it). */
	private static boolean seenOff;

	/** Switches the free camera off and leaves a temporary waypoint where the camera was. */
	static void leave(Minecraft client) {
		CameraEntity camera = CameraEntity.getCamera();
		if (camera != null && client.level != null) {
			if (temporary != null) Markers.remove(temporary);
			temporary = Markers.add(client.level.dimension().identifier().toString(), camera.blockPosition());
			seenOff = false;
		}
		fi.dy.masa.tweakeroo.config.FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);
	}

	/** For the dev test. */
	public static Markers.Marker temporary() {
		return temporary;
	}

	private static void tickTemporary() {
		if (temporary == null) return;
		if (!Markers.list().contains(temporary)) {
			// walked up to it (or removed by hand)
			temporary = null;
			return;
		}
		boolean on = CameraEntity.getCamera() != null;
		if (!on) {
			seenOff = true;
		} else if (seenOff) {
			// the free camera is on again: not needed any more
			Markers.remove(temporary);
			temporary = null;
		}
	}

	private static void alert(Minecraft client) {
		long now = System.currentTimeMillis();
		if (now - lastAlert < 1000) {
			return;
		}
		lastAlert = now;
		alerts++;
		float health = client.player.getHealth();
		client.gui.setOverlayMessage(Component.translatable("freecamplus.hurt", String.format("%.1f", health / 2f))
				.withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
		client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL.value(), 1.4F, 1.0F));
		//? if >=1.21.11 <26.1
		if (FreecamHotkeys.LEAVE_WHEN_HURT.getBooleanValue()) leave(client);
	}
}
