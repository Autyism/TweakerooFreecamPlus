package io.github.autyism.freecamplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
//? if >=1.21.11 <26.1 {
import io.github.autyism.freecamplus.FreecamInteract;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}

/** Tweakeroo blocks every click in the free camera; with the camera's own target (see FreecamInteract) they go through */
@Pseudo
@Mixin(targets = "fi.dy.masa.tweakeroo.util.CameraUtils", remap = false)
public class TweakerooInputsMixin {
	//? if >=1.21.11 <26.1 {
	@Inject(method = "shouldPreventPlayerInputs", at = @At("RETURN"), cancellable = true, require = 0)
	private static void freecamplus$allowClicks(CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && FreecamInteract.active(Minecraft.getInstance())) cir.setReturnValue(false);
	}
	//?}
}
