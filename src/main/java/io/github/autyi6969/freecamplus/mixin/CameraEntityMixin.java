package io.github.autyi6969.freecamplus.mixin;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyi6969.freecamplus.SprintSpeed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Tweakeroo multiplies the forward speed by a fixed 3.0 while sprinting; use ours instead. */
@Mixin(value = CameraEntity.class, remap = false)
public class CameraEntityMixin {
	@ModifyConstant(method = "movementTick", constant = @Constant(doubleValue = 3.0))
	private static double freecamplus$sprintFactor(double original) {
		return SprintSpeed.factor();
	}
}
