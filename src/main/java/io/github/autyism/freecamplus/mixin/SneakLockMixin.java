package io.github.autyism.freecamplus.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.11 <26.1 {
import io.github.autyism.freecamplus.FreecamInteract;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}

/** During a sneaking click from the free camera the game's own click handling sees your character sneaking too (placing against a chest places) */
@Mixin(LocalPlayer.class)
public class SneakLockMixin {
	//? if >=1.21.11 <26.1 {
	@Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
	private void freecamplus$sneakLock(CallbackInfoReturnable<Boolean> cir) {
		if (FreecamInteract.sneakingFor((LocalPlayer) (Object) this)) cir.setReturnValue(true);
	}
	//?}
}
