//? if <1.21.11 {
/*package io.github.autyism.freecamplus.render.legacy;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.autyism.freecamplus.FreecamPlus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.TreeMap;

/^*
 * Draws the shapes made with {@link Gizmos} on the versions before 1.21.11, the way 1.21.11 draws its own:
 * ordinary shapes together with the game's debug shapes, "always on top" shapes last in the frame over a
 * cleared depth buffer. Within each, the fully opaque lines go first, then the see-through ones.
 * (The same drawing as QoL Bundle's, reduced to the lines this mod uses.)
 ^/
public final class GizmoRenderer {
	/^* 1.21.11's line pipelines: the opaque one writes depth, the see-through one does not. ^/
	private static final RenderPipeline TRANSLUCENT_LINES_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(ResourceLocation.fromNamespaceAndPath(FreecamPlus.MOD_ID, "pipeline/gizmo_lines_translucent"))
			.withDepthWrite(false).build();
	/^* Line width is part of a render type before 1.21.11, so there is one per width (and see-through or not). ^/
	private static final Map<Float, RenderType> OPAQUE_LINES = new HashMap<>();
	private static final Map<Float, RenderType> TRANSLUCENT_LINES = new HashMap<>();
	/^* 1.21.11's lines move this close to the camera at most; whatever is nearer is cut off. ^/
	private static final float NEAR_LIMIT = -0.05F;

	private static final List<Shape> SHAPES = new ArrayList<>();

	private GizmoRenderer() {
	}

	/^* Forgets the shapes of the previous frame; this frame's are added right after. ^/
	public static void beginFrame() {
		SHAPES.clear();
	}

	static Shape newShape() {
		Shape shape = new Shape();
		SHAPES.add(shape);
		return shape;
	}

	/^* Ordinary shapes, drawn when the game draws its own debug shapes. ^/
	public static void drawStandard(PoseStack poseStack, MultiBufferSource.BufferSource buffers, double cameraX, double cameraY, double cameraZ) {
		draw(false, poseStack, buffers, new Vec3(cameraX, cameraY, cameraZ));
	}

	/^* "Always on top" shapes, last in the frame: like 1.21.11, the depth buffer is cleared for them first. ^/
	public static void drawOnTop(PoseStack poseStack, MultiBufferSource.BufferSource buffers, double cameraX, double cameraY, double cameraZ) {
		boolean any = false;
		for (Shape shape : SHAPES) {
			any |= shape.onTop;
		}
		if (any) {
			RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(Minecraft.getInstance().getMainRenderTarget().getDepthTexture(), 1.0);
			draw(true, poseStack, buffers, new Vec3(cameraX, cameraY, cameraZ));
		}
		SHAPES.clear();
	}

	private static void draw(boolean onTop, PoseStack poseStack, MultiBufferSource.BufferSource buffers, Vec3 camera) {
		List<Line> opaque = new ArrayList<>();
		List<Line> translucent = new ArrayList<>();
		for (Shape shape : SHAPES) {
			if (shape.onTop == onTop) {
				for (Line line : shape.lines) {
					(isOpaque(line.color) ? opaque : translucent).add(line);
				}
			}
		}
		if (opaque.isEmpty() && translucent.isEmpty()) {
			return;
		}
		Camera view = Minecraft.getInstance().gameRenderer.getMainCamera();
		Matrix4f viewRotation = new Matrix4f().rotation(view.rotation().conjugate(new Quaternionf()));
		renderLines(opaque, true, buffers, poseStack.last(), camera, viewRotation);
		renderLines(translucent, false, buffers, poseStack.last(), camera, viewRotation);
		buffers.endLastBatch();
	}

	private static RenderType lines(float width, boolean opaque) {
		Map<Float, RenderType> types = opaque ? OPAQUE_LINES : TRANSLUCENT_LINES;
		return types.computeIfAbsent(width, w -> RenderType.create("freecamplus_gizmo_lines_" + (opaque ? "" : "translucent_") + w, 1536,
				opaque ? RenderPipelines.LINES : TRANSLUCENT_LINES_PIPELINE,
				RenderType.CompositeState.builder()
						.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(w)))
						.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
						.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
						.createCompositeState(false)));
	}

	private static boolean isOpaque(int color) {
		return (color >>> 24) == 255;
	}

	private static void renderLines(List<Line> lines, boolean opaque, MultiBufferSource.BufferSource buffers, PoseStack.Pose pose,
			Vec3 camera, Matrix4f viewRotation) {
		// Grouped by width (each width is its own render type here), widths in a fixed order.
		Map<Float, List<Line>> byWidth = new TreeMap<>();
		for (Line line : lines) {
			byWidth.computeIfAbsent(line.width, w -> new ArrayList<>()).add(line);
		}
		Vector4f start = new Vector4f();
		Vector4f end = new Vector4f();
		Vector4f startInView = new Vector4f();
		Vector4f endInView = new Vector4f();
		Vector4f cut = new Vector4f();
		for (Map.Entry<Float, List<Line>> entry : byWidth.entrySet()) {
			VertexConsumer consumer = buffers.getBuffer(lines(entry.getKey(), opaque));
			for (Line line : entry.getValue()) {
				start.set(line.start.x - camera.x, line.start.y - camera.y, line.start.z - camera.z, 1.0);
				end.set(line.end.x - camera.x, line.end.y - camera.y, line.end.z - camera.z, 1.0);
				start.mul(viewRotation, startInView);
				end.mul(viewRotation, endInView);
				boolean startTooNear = startInView.z > NEAR_LIMIT;
				boolean endTooNear = endInView.z > NEAR_LIMIT;
				if (startTooNear && endTooNear) {
					continue;
				}
				if (startTooNear || endTooNear) {
					// One end is behind the camera (or nearly): keep only the part in front of it.
					float span = endInView.z - startInView.z;
					if (Math.abs(span) < 1.0E-9F) {
						continue;
					}
					float t = Math.max(0.0F, Math.min(1.0F, (NEAR_LIMIT - startInView.z) / span));
					start.lerp(end, t, cut);
					if (startTooNear) {
						start.set(cut);
					} else {
						end.set(cut);
					}
				}
				float dx = end.x - start.x;
				float dy = end.y - start.y;
				float dz = end.z - start.z;
				consumer.addVertex(pose, start.x, start.y, start.z).setColor(line.color).setNormal(pose, dx, dy, dz);
				consumer.addVertex(pose, end.x, end.y, end.z).setColor(line.color).setNormal(pose, dx, dy, dz);
			}
		}
	}

	record Line(Vec3 start, Vec3 end, int color, float width) {
	}

	/^* One shape as the mod sees it: its lines, and whether it is drawn on top. ^/
	static final class Shape implements GizmoProperties {
		private final List<Line> lines = new ArrayList<>();
		private boolean onTop;

		@Override
		public GizmoProperties setAlwaysOnTop() {
			onTop = true;
			return this;
		}

		void line(Vec3 start, Vec3 end, int color, float width) {
			lines.add(new Line(start, end, color, width));
		}
	}
}
*///?}
