package io.github.autyism.freecamplus.mixin;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyism.freecamplus.SprintSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** In the free camera, sprint key + mouse wheel changes the sprint speed instead of the hotbar slot. */
@Mixin(MouseHandler.class)
public class MouseMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void freecamplus$sprintSpeedScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (window != minecraft.getWindow().handle() || minecraft.screen != null || vertical == 0
				|| CameraEntity.getCamera() == null || !SprintSpeed.sprintKeyHeld(minecraft)) {
			return;
		}
		SprintSpeed.step(vertical > 0 ? 1 : -1);
		ci.cancel();
	}
}
