package io.github.autyism.freecamplus.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.11 <26.1 {
import io.github.autyism.freecamplus.FreecamInteract;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

/** In the free camera the crosshair target is what the camera looks at (within the character's reach) */
@Mixin(GameRenderer.class)
public class FreecamPickMixin {
	//? if >=1.21.11 <26.1 {
	@Inject(method = "pick(F)V", at = @At("TAIL"))
	private void freecamplus$cameraTarget(float partialTick, CallbackInfo ci) {
		FreecamInteract.repick(Minecraft.getInstance(), partialTick);
	}
	//?}
}
