package io.github.autyi6969.freecamplus.mixin;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Tweakeroo's own "sprinting" state of the free camera (latched until you stop moving forward). */
@Mixin(value = CameraEntity.class, remap = false)
public interface CameraEntityAccessor {
	@Accessor("sprinting")
	static boolean freecamplus$isSprinting() {
		throw new AssertionError();
	}
}
