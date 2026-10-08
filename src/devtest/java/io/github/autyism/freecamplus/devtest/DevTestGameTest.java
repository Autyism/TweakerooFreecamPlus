package io.github.autyism.freecamplus.devtest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Runs {@link DevTest} in a fresh world made by Fabric's client game test, so the test works the same
 * on every Minecraft version without a saved world (gradlew :&lt;version&gt;:runClientGameTest).
 */
public class DevTestGameTest implements FabricClientGameTest {
	private static final int MAX_TICKS = 20 * 60 * 5;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getClientWorld().waitForChunksRender();
			DevTest.worldReady = true;
			context.waitFor(client -> DevTest.finished, MAX_TICKS);
		}
		if (DevTest.failureCount > 0) {
			throw new AssertionError("[DevTest] " + DevTest.failureCount + " check(s) failed, see the [DevTest] lines");
		}
	}
}
