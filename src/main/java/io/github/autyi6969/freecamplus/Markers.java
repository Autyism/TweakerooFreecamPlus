package io.github.autyi6969.freecamplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The waypoints, remembered per world / server in config/tweakeroo_freecam_plus/worlds/&lt;world&gt;.json
 * (same idea and file layout as QoL Bundle's Freecam markers, but its own folder).
 */
public final class Markers {
	public static final int MAX = 10;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public record Marker(int number, String dimension, BlockPos pos) {
	}

	private static final List<Marker> LIST = new ArrayList<>();
	private static int nextNumber = 1;
	@Nullable
	private static String worldId;

	private Markers() {
	}

	public static List<Marker> list() {
		return LIST;
	}

	public static Marker add(String dimension, BlockPos pos) {
		LIST.removeIf(marker -> marker.dimension.equals(dimension) && marker.pos.equals(pos));
		while (LIST.size() >= MAX) {
			LIST.remove(0); // the oldest makes room
		}
		Marker marker = new Marker(nextNumber++, dimension, pos.toImmutable());
		LIST.add(marker);
		save();
		return marker;
	}

	public static void remove(Marker marker) {
		if (LIST.remove(marker)) {
			save();
		}
	}

	/** Removes the markers of this dimension within 3 blocks of the player; called every tick. */
	public static void removeArrived(String dimension, BlockPos player) {
		if (LIST.removeIf(marker -> marker.dimension.equals(dimension) && marker.pos.getSquaredDistance(player) <= 3 * 3)) {
			save();
		}
	}

	/** Loads the markers of the world the client is in now (only does work when the world changed). */
	public static void sync(MinecraftClient client) {
		String id = client.world == null ? null : computeId(client);
		if (Objects.equals(id, worldId)) {
			return;
		}
		worldId = id;
		LIST.clear();
		nextNumber = 1;
		if (id == null || !Files.exists(file(id))) {
			return;
		}
		try {
			JsonElement parsed = JsonParser.parseString(Files.readString(file(id), StandardCharsets.UTF_8));
			for (JsonElement element : parsed.getAsJsonObject().getAsJsonArray("markers")) {
				try {
					JsonObject json = element.getAsJsonObject();
					Marker marker = new Marker(json.get("n").getAsInt(), json.get("dimension").getAsString(),
							new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt()));
					LIST.add(marker);
					nextNumber = Math.max(nextNumber, marker.number + 1);
				} catch (RuntimeException ignored) {
					// skip a damaged entry
				}
			}
		} catch (Exception e) {
			FreecamPlus.LOGGER.error("Could not read waypoints {}", file(id), e);
		}
	}

	private static void save() {
		if (worldId == null) {
			return;
		}
		JsonArray array = new JsonArray();
		for (Marker marker : LIST) {
			JsonObject json = new JsonObject();
			json.addProperty("n", marker.number);
			json.addProperty("dimension", marker.dimension);
			json.addProperty("x", marker.pos.getX());
			json.addProperty("y", marker.pos.getY());
			json.addProperty("z", marker.pos.getZ());
			array.add(json);
		}
		JsonObject root = new JsonObject();
		root.add("markers", array);
		Path path = file(worldId);
		try {
			Files.createDirectories(path.getParent());
			Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
			Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
			Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			FreecamPlus.LOGGER.error("Could not write waypoints {}", path, e);
		}
	}

	private static Path file(String id) {
		return FabricLoader.getInstance().getConfigDir().resolve(FreecamPlus.MOD_ID).resolve("worlds").resolve(id + ".json");
	}

	private static String computeId(MinecraftClient client) {
		String raw;
		if (client.isIntegratedServerRunning() && client.getServer() != null) {
			Path folder = client.getServer().getSavePath(WorldSavePath.ROOT).toAbsolutePath().normalize();
			raw = "local_" + folder.getFileName();
		} else {
			ServerInfo server = client.getCurrentServerEntry();
			raw = "server_" + (server != null ? server.address : "unknown");
		}
		return raw.replaceAll("[^A-Za-z0-9._-]", "_");
	}
}
