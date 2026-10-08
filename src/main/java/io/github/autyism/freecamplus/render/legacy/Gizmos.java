//? if <1.21.11 {
/*package io.github.autyism.freecamplus.render.legacy;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/^*
 * Minecraft 1.21.11 added "gizmos": simple shapes drawn in the world, the way the F3 debug views draw.
 * The waypoints are drawn with them. For the versions before 1.21.11 this class offers the same calls
 * (the ones this mod uses), and {@link GizmoRenderer} draws the shapes the same way.
 ^/
public final class Gizmos {
	private Gizmos() {
	}

	public static GizmoProperties line(Vec3 start, Vec3 end, int color, float width) {
		GizmoRenderer.Shape shape = GizmoRenderer.newShape();
		shape.line(start, end, color, width);
		return shape;
	}

	/^* The twelve edges of a box. ^/
	public static GizmoProperties cuboid(AABB box, GizmoStyle style) {
		GizmoRenderer.Shape shape = GizmoRenderer.newShape();
		double[] xs = {box.minX, box.maxX};
		double[] ys = {box.minY, box.maxY};
		double[] zs = {box.minZ, box.maxZ};
		for (double y : ys) {
			for (double z : zs) {
				shape.line(new Vec3(box.minX, y, z), new Vec3(box.maxX, y, z), style.stroke(), style.strokeWidth());
			}
		}
		for (double x : xs) {
			for (double z : zs) {
				shape.line(new Vec3(x, box.minY, z), new Vec3(x, box.maxY, z), style.stroke(), style.strokeWidth());
			}
			for (double y : ys) {
				shape.line(new Vec3(x, y, box.minZ), new Vec3(x, y, box.maxZ), style.stroke(), style.strokeWidth());
			}
		}
		return shape;
	}
}
*///?}
