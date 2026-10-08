package io.github.autyism.freecamplus;

import net.minecraft.util.Util;

/** Triple tap of the sprint key (Ctrl) in the free camera: point at your own body for a few seconds. */
public final class SelfPointer {
	private static final long TAP_WINDOW_MS = 700;
	private static final long SHOW_MS = 5000;

	private static final long[] taps = new long[3];
	private static int tapCount;
	private static long visibleUntil;

	private SelfPointer() {
	}

	public static void tap() {
		long now = Util.getMillis();
		taps[tapCount % 3] = now;
		tapCount++;
		long oldest = taps[tapCount % 3]; // the third-last tap
		if (tapCount >= 3 && now - oldest <= TAP_WINDOW_MS) {
			visibleUntil = now + SHOW_MS;
			tapCount = 0;
		}
	}

	public static boolean visible() {
		return Util.getMillis() < visibleUntil;
	}

	public static void hide() {
		visibleUntil = 0;
		tapCount = 0;
	}
}
