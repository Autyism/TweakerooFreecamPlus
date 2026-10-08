//? if >=1.21.9 <1.21.11 {
/*package io.github.autyism.freecamplus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.autyism.freecamplus.FreecamPlus;
import io.github.autyism.freecamplus.render.legacy.GizmoRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/^*
 * Before 1.21.11 (here 1.21.9 and 1.21.10): adds the waypoint shapes when the game draws its own debug shapes
 * and draws them with {@link GizmoRenderer}, the "always on top" ones after its last debug drawing in the frame.
 * (Fabric API for 1.21.9 has no world drawing events, so the mod has its own hook here.)
 ^/
@Mixin(DebugRenderer.class)
public class LegacyDebugRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void freecamplus$drawShapes(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource buffers,
			double cameraX, double cameraY, double cameraZ, boolean late, CallbackInfo ci) {
		if (!late) {
			GizmoRenderer.beginFrame();
			FreecamPlus.renderWorld();
			GizmoRenderer.drawStandard(poseStack, buffers, cameraX, cameraY, cameraZ);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void freecamplus$drawShapesOnTop(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource buffers,
			double cameraX, double cameraY, double cameraZ, boolean late, CallbackInfo ci) {
		if (late) {
			GizmoRenderer.drawOnTop(poseStack, buffers, cameraX, cameraY, cameraZ);
		}
	}
}
*///?} elif <1.21.9 {
/*package io.github.autyism.freecamplus.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.autyism.freecamplus.FreecamPlus;
import io.github.autyism.freecamplus.render.legacy.GizmoRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/^*
 * Before 1.21.9: adds the waypoint shapes when the game draws its own debug shapes and draws them with
 * {@link GizmoRenderer}, the "always on top" ones after its last debug drawing in the frame.
 ^/
@Mixin(DebugRenderer.class)
public class LegacyDebugRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void freecamplus$drawShapes(PoseStack poseStack, Frustum frustum, MultiBufferSource.BufferSource buffers,
			double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		GizmoRenderer.beginFrame();
		FreecamPlus.renderWorld();
		GizmoRenderer.drawStandard(poseStack, buffers, cameraX, cameraY, cameraZ);
	}

	@Inject(method = "renderAfterTranslucents", at = @At("TAIL"))
	private void freecamplus$drawShapesOnTop(PoseStack poseStack, MultiBufferSource.BufferSource buffers,
			double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		GizmoRenderer.drawOnTop(poseStack, buffers, cameraX, cameraY, cameraZ);
	}
}
*///?}
