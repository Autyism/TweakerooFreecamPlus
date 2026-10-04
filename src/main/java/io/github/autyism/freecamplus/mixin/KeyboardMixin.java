package io.github.autyism.freecamplus.mixin;

import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyism.freecamplus.SelfPointer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Keyboard;

/** Counts real presses (not key-repeats) of the sprint key in the free camera, for the triple tap. */
@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Inject(method = "onKey", at = @At("HEAD"))
	private void freecamplus$sprintTaps(long window, int action, KeyInput input, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (action != GLFW.GLFW_PRESS || CameraEntity.getCamera() == null || client.currentScreen != null
				|| window != client.getWindow().getHandle()) {
			return;
		}
		InputUtil.Key sprint = KeyBindingHelper.getBoundKeyOf(client.options.sprintKey);
		if (sprint.getCategory() == InputUtil.Type.KEYSYM && sprint.getCode() == input.key()) {
			SelfPointer.tap();
		}
	}
}
