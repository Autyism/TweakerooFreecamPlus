package io.github.autyism.freecamplus;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
 * Sharing waypoints through chat. Shift + middle click on a waypoint (in the free camera) says it in chat as
 * "[Waypoint #3] 120 64 -40 minecraft:overworld", readable by anyone. Players who have this mod get a line under it
 * with "[Add waypoint]"; a click adds it to their own waypoints (client command /fcpwaypoint, nothing is sent).
 */
public final class SharedWaypoints {
	private static final Pattern SHARED = Pattern.compile("\\[Waypoint #?\\d*] (-?\\d+) (-?\\d+) (-?\\d+) ([a-z0-9_.-]+:[a-z0-9_./-]+)");
	/** For the dev test: the last offer shown (the command a click runs), or null. */
	public static String lastOffer;
	/** Lines to add under the chat message they belong to: added on the next tick, after the message itself. */
	private static final java.util.List<Component> PENDING = new java.util.ArrayList<>();

	private SharedWaypoints() {
	}

	static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(literal("fcpwaypoint")
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

	/** Says the waypoint in chat. */
	static void share(Minecraft client, Markers.Marker marker) {
		if (client.player == null) {
			return;
		}
		BlockPos p = marker.pos();
		client.player.connection.sendChat("[Waypoint #" + marker.number() + "] " + p.getX() + " " + p.getY() + " " + p.getZ() + " " + marker.dimension());
		client.gui.setOverlayMessage(Component.translatable("freecamplus.share.sent", marker.number()), false);
	}

	private static void offer(String text) {
		Matcher m = SHARED.matcher(text);
		if (!m.find()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		String command = "/fcpwaypoint " + m.group(1) + " " + m.group(2) + " " + m.group(3) + " " + m.group(4);
		lastOffer = command;
		MutableComponent button = Component.translatable("freecamplus.share.add").withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE)
				.withUnderlined(true)
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(Component.translatable("freecamplus.share.add.hover", m.group(1), m.group(2), m.group(3)))));
		synchronized (PENDING) {
			PENDING.add(Component.literal("  ").append(button));
		}
	}

	/** End of a client tick: the offers of this tick's messages go in under them. */
	static void tick(Minecraft client) {
		java.util.List<Component> lines;
		synchronized (PENDING) {
			if (PENDING.isEmpty()) return;
			lines = new java.util.ArrayList<>(PENDING);
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
