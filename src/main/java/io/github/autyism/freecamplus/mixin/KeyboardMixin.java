package io.github.autyism.freecamplus.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import io.github.autyism.freecamplus.SelfPointer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
//? if >=1.21.9
import net.minecraft.client.input.KeyEvent;
//? if <26.3
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Counts real presses (not key-repeats) of the sprint key in the free camera, for the triple tap. */
@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
	// From 1.21.9 a key press is a KeyEvent; before, it was (key, scancode, action, modifiers)
	//? if >=1.21.9 {
	@Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At("HEAD"))
	private void freecamplus$sprintTaps(long window, int action, KeyEvent input, CallbackInfo ci) {
		int key = input.key();
	//?} else {
	/*@Inject(method = "keyPress(JIIII)V", at = @At("HEAD"))
	private void freecamplus$sprintTaps(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
	*///?}
		Minecraft client = Minecraft.getInstance();
		if (action != GLFW.GLFW_PRESS || CameraEntity.getCamera() == null || client.screen != null
				|| window != client.getWindow().handle()) {
			return;
		}
		InputConstants.Key sprint = KeyBindingHelper.getBoundKeyOf(client.options.keySprint);
		if (sprint.getType() == InputConstants.Type.KEYSYM && sprint.getValue() == key) {
			SelfPointer.tap();
		}
	}
}
