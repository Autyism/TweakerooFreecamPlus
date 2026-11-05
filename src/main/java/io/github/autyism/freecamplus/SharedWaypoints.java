package io.github.autyism.freecamplus;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//? if >=26.1 {
/*import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
*///?} else {
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
//?}
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Sharing waypoints through chat, in the format Xaero's Minimap uses (Conflux Map reads it too), so players with
 * either map can add a shared waypoint to theirs. Shift + middle click on a waypoint in the free camera shares it;
 * /fcpwaypoint list lists every waypoint of every dimension with [Share] and [Remove]. Received waypoints in Xaero's,
 * Conflux Map's or this mod's older format get an [Add waypoint] line under them; a click adds it to this mod's
 * waypoints (client command /fcpwaypoint, nothing is sent).
 */
public final class SharedWaypoints {
	private static final Pattern OURS = Pattern.compile("\\[Waypoint #?\\d*] (-?\\d+) (-?\\d+) (-?\\d+) ([a-z0-9_.-]+:[a-z0-9_./-]+)");
	private static final Pattern XAERO = Pattern.compile("xaero-waypoint:([^:]*):([^:]*):(-?\\d+):(~|-?\\d+):(-?\\d+):(\\d+):(true|false):(-?\\d+)(?::(Internal-\\S+?-waypoints))?");
	private static final Pattern CONFLUX = Pattern.compile("\\[Conflux Map] (.*?) \\| ([a-z0-9_.-]+:[a-z0-9/._-]+) \\| X: (-?[\\d.]+), Y: (-?[\\d.]+), Z: (-?[\\d.]+)");
	/** Xaero's light purple: the colour of this mod's waypoints. */
	private static final int XAERO_COLOR = 13;
	/** For the dev test: the last offer shown (the command a click runs), or null. */
	public static String lastOffer;
	/** Lines to add under the chat message they belong to: added on the next tick, after the message itself. */
	private static final List<Component> PENDING = new ArrayList<>();

	private SharedWaypoints() {
	}

	static void register() {
		// the in-game manual (the settings screen has a button for it on 1.21.11; other versions have no settings screen)
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(literal("fcpmanual").executes(ctx -> {
			Minecraft mc = Minecraft.getInstance();
			// after the chat screen has closed
			mc.schedule(() -> {
				//? if >=26.2 {
				/*mc.gui.setScreen(new ManualScreen(null));
				*///?} else
				mc.setScreen(new ManualScreen(null));
			});
			return 1;
		})));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(literal("fcpwaypoint")
				.then(literal("list").executes(ctx -> {
					list();
					return 1;
				}))
				.then(literal("share").then(argument("number", IntegerArgumentType.integer(1)).executes(ctx -> {
					Markers.Marker m = find(IntegerArgumentType.getInteger(ctx, "number"));
					if (m != null) share(Minecraft.getInstance(), m);
					return m != null ? 1 : 0;
				})))
				.then(literal("remove").then(argument("number", IntegerArgumentType.integer(1)).executes(ctx -> {
					Markers.Marker m = find(IntegerArgumentType.getInteger(ctx, "number"));
					if (m != null) {
						Markers.remove(m);
						Minecraft client = Minecraft.getInstance();
						client.gui.setOverlayMessage(Component.translatable("freecamplus.marker.removed", m.number()), false);
					}
					return m != null ? 1 : 0;
				})))
				.then(argument("x", IntegerArgumentType.integer())
						.then(argument("y", IntegerArgumentType.integer())
								.then(argument("z", IntegerArgumentType.integer())
										.then(argument("dimension", StringArgumentType.greedyString())
												.executes(ctx -> {
													BlockPos pos = new BlockPos(IntegerArgumentType.getInteger(ctx, "x"),
															IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"));
													add(StringArgumentType.getString(ctx, "dimension").trim(), pos);
													return 1;
												})))))));
		ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
			Minecraft client = Minecraft.getInstance();
			//? if >=1.21.9 {
			boolean mine = sender != null && client.player != null && sender.id().equals(client.player.getUUID());
			//?} else
			/*boolean mine = sender != null && client.player != null && sender.getId().equals(client.player.getUUID());*/
			if (!mine) offer(message.getString());
		});
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (!overlay) offer(message.getString());
		});
	}

	private static Markers.Marker find(int number) {
		for (Markers.Marker m : Markers.list()) if (m.number() == number) return m;
		return null;
	}

	/** "Internal-the-nether-waypoints" for minecraft:the_nether (Xaero's names), "Internal-dim%mod$name-waypoints" for others. */
	public static String xaeroDimension(String dimension) {
		return switch (dimension) {
			case "minecraft:overworld" -> "Internal-overworld-waypoints";
			case "minecraft:the_nether" -> "Internal-the-nether-waypoints";
			case "minecraft:the_end" -> "Internal-the-end-waypoints";
			default -> {
				int colon = dimension.indexOf(':');
				yield "Internal-dim%" + dimension.substring(0, colon) + "$" + dimension.substring(colon + 1).replace('/', '%') + "-waypoints";
			}
		};
	}

	public static String fromXaeroDimension(String xaero, String current) {
		if (xaero == null) return current;
		String inner = xaero.substring("Internal-".length(), xaero.length() - "-waypoints".length());
		return switch (inner) {
			case "overworld" -> "minecraft:overworld";
			case "the-nether" -> "minecraft:the_nether";
			case "the-end" -> "minecraft:the_end";
			default -> {
				if (inner.startsWith("dim%") && inner.indexOf('$') > 0) {
					String ns = inner.substring(4, inner.indexOf('$'));
					yield ns + ":" + inner.substring(inner.indexOf('$') + 1).replace('%', '/');
				}
				yield current;
			}
		};
	}

	/** The chat line for a waypoint, in Xaero's format. */
	public static String message(Markers.Marker marker) {
		BlockPos p = marker.pos();
		return "xaero-waypoint:Waypoint " + marker.number() + ":" + marker.number() + ":" + p.getX() + ":" + p.getY() + ":" + p.getZ()
				+ ":" + XAERO_COLOR + ":false:0:" + xaeroDimension(marker.dimension());
	}

	/** Says the waypoint in chat. */
	static void share(Minecraft client, Markers.Marker marker) {
		if (client.player == null) {
			return;
		}
		client.player.connection.sendChat(message(marker));
		client.gui.setOverlayMessage(Component.translatable("freecamplus.share.sent", marker.number()), false);
	}

	/** Every waypoint (all dimensions) in chat, each with [Share] and [Remove]. */
	public static void list() {
		List<Component> lines = new ArrayList<>();
		if (Markers.list().isEmpty()) {
			lines.add(Component.translatable("freecamplus.list.empty").withStyle(ChatFormatting.GRAY));
		} else {
			lines.add(Component.translatable("freecamplus.list.title").withStyle(ChatFormatting.LIGHT_PURPLE));
			for (Markers.Marker m : Markers.list()) {
				BlockPos p = m.pos();
				MutableComponent line = Component.literal(" #" + m.number() + "  " + p.getX() + " " + p.getY() + " " + p.getZ() + "  " + m.dimension() + "  ");
				line.append(button("freecamplus.list.share", "/fcpwaypoint share " + m.number(), ChatFormatting.AQUA));
				line.append(Component.literal(" "));
				line.append(button("freecamplus.list.remove", "/fcpwaypoint remove " + m.number(), ChatFormatting.RED));
				lines.add(line);
			}
		}
		synchronized (PENDING) {
			PENDING.addAll(lines);
		}
	}

	private static MutableComponent button(String key, String command, ChatFormatting color) {
		return Component.translatable(key).withStyle(style -> style.withColor(color).withUnderlined(true)
				.withClickEvent(new ClickEvent.RunCommand(command)));
	}

	private static void offer(String text) {
		Minecraft client = Minecraft.getInstance();
		String here = client.level == null ? "minecraft:overworld" : client.level.dimension().identifier().toString();
		String x, y, z, dimension;
		Matcher m;
		if ((m = XAERO.matcher(text)).find()) {
			x = m.group(3);
			y = m.group(4).equals("~") ? "64" : m.group(4);
			z = m.group(5);
			dimension = fromXaeroDimension(m.group(9), here);
		} else if ((m = CONFLUX.matcher(text)).find()) {
			x = String.valueOf((int) Math.floor(Double.parseDouble(m.group(3))));
			y = String.valueOf((int) Math.floor(Double.parseDouble(m.group(4))));
			z = String.valueOf((int) Math.floor(Double.parseDouble(m.group(5))));
			dimension = m.group(2);
		} else if ((m = OURS.matcher(text)).find()) {
			x = m.group(1);
			y = m.group(2);
			z = m.group(3);
			dimension = m.group(4);
		} else {
			return;
		}
		String command = "/fcpwaypoint " + x + " " + y + " " + z + " " + dimension;
		lastOffer = command;
		MutableComponent button = Component.translatable("freecamplus.share.add").withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE)
				.withUnderlined(true)
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(Component.translatable("freecamplus.share.add.hover", x, y, z, dimension))));
		synchronized (PENDING) {
			PENDING.add(Component.literal("  ").append(button));
		}
	}

	/** End of a client tick: the offers of this tick's messages go in under them. */
	static void tick(Minecraft client) {
		List<Component> lines;
		synchronized (PENDING) {
			if (PENDING.isEmpty()) return;
			lines = new ArrayList<>(PENDING);
			PENDING.clear();
		}
		for (Component line : lines) {
			//? if >=26.2 {
			/*client.gui.hud.getChat().addClientSystemMessage(line);
			*///?} elif >=26.1 {
			/*client.gui.getChat().addClientSystemMessage(line);
			*///?} else
			client.gui.getChat().addMessage(line);
		}
	}

	static void add(String dimension, BlockPos pos) {
		Markers.Marker marker = Markers.add(dimension, pos);
		Minecraft client = Minecraft.getInstance();
		client.gui.setOverlayMessage(Component.translatable("freecamplus.marker.added", marker.number(), pos.getX(), pos.getY(), pos.getZ()), false);
	}
}
