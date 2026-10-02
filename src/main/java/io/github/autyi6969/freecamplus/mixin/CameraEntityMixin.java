package io.github.autyi6969.freecamplus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import fi.dy.masa.tweakeroo.util.CameraPreset;
import io.github.autyi6969.freecamplus.SelfPointer;
import io.github.autyi6969.freecamplus.SprintSpeed;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CameraEntity.class)
public class CameraEntityMixin {
	/** Tweakeroo multiplies the forward speed by a fixed 3.0 while sprinting; use ours instead. */
	@ModifyConstant(method = "movementTick", remap = false, constant = @Constant(doubleValue = 3.0))
	private static double freecamplus$sprintFactor(double original) {
		return SprintSpeed.factor();
	}

	/**
	 * Tweakeroo asks {@code sprintKey.isPressed()}. With "Toggle Sprint" on, that value flips on every
	 * key-repeat while Ctrl is held (sprint flickered ON/OFF). Use the physical key instead: sprint
	 * while Ctrl is held, and (Tweakeroo's own rule) it stays on until you stop moving forward.
	 */
	@ModifyExpressionValue(method = "movementTick", remap = false, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 0, remap = true))
	private static boolean freecamplus$physicalSprint(boolean original) {
		return SprintSpeed.sprintKeyHeld(MinecraftClient.getInstance());
	}

	/** Every time the free camera is switched on it starts at the normal speed (x3). */
	@Inject(method = "setCameraState", remap = false, at = @At("HEAD"))
	private static void freecamplus$onToggle(boolean enabled, CameraPreset preset, CallbackInfo ci) {
		SprintSpeed.reset();
		SelfPointer.hide();
	}
}
