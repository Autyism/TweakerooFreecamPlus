package io.github.autyism.freecamplus.mixin;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.StickyKeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * With "Toggle Sprint" on, every Ctrl press (and every key-repeat while it is held) flips the body's
 * sprint toggle. In the free camera Ctrl is used a lot (speed, triple tap), so the body's toggle is
 * frozen there and is exactly as before when you leave the free camera.
 */
@Mixin(StickyKeyBinding.class)
public class StickyKeyBindingMixin {
	@Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
	private void freecamplus$freezeSprintToggle(boolean pressed, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (pressed && CameraEntity.getCamera() != null && client.options != null
				&& (Object) this == client.options.sprintKey && client.options.getSprintToggled().getValue()) {
			ci.cancel();
		}
	}
}
