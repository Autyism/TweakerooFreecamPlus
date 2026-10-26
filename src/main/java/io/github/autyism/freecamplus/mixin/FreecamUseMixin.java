package io.github.autyism.freecamplus.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.11 <26.1 {
import io.github.autyism.freecamplus.FreecamInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}

/**
 * Clicks from the free camera: placing faces the camera's way, item uses aim at the camera's block, sneak held sneaks
 * the character for the click (see FreecamInteract); everything is put back after the click.
 */
@Mixin(MultiPlayerGameMode.class)
public class FreecamUseMixin {
	//? if >=1.21.11 <26.1 {
	@Inject(method = "useItemOn", at = @At("HEAD"))
	private void freecamplus$placeBegin(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.beginPlace(Minecraft.getInstance());
	}

	@Inject(method = "useItemOn", at = @At("RETURN"))
	private void freecamplus$placeEnd(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.end(Minecraft.getInstance());
	}

	@Inject(method = "useItem", at = @At("HEAD"))
	private void freecamplus$useBegin(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.beginUse(Minecraft.getInstance());
	}

	@Inject(method = "useItem", at = @At("RETURN"))
	private void freecamplus$useEnd(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.end(Minecraft.getInstance());
	}

	@Inject(method = "interact", at = @At("HEAD"))
	private void freecamplus$entityBegin(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.beginPlace(Minecraft.getInstance());
	}

	@Inject(method = "interact", at = @At("RETURN"))
	private void freecamplus$entityEnd(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.end(Minecraft.getInstance());
	}

	@Inject(method = "interactAt", at = @At("HEAD"))
	private void freecamplus$entityAtBegin(Player player, Entity entity, EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.beginPlace(Minecraft.getInstance());
	}

	@Inject(method = "interactAt", at = @At("RETURN"))
	private void freecamplus$entityAtEnd(Player player, Entity entity, EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		FreecamInteract.end(Minecraft.getInstance());
	}
	//?}
}
