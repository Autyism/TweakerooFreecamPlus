package io.github.autyism.freecamplus;

//? if >=1.21.11 <26.1 {
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.malilib.util.data.json.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
//?}

/**
 * The free camera's hotkeys (MaLiLib, changeable in the settings: Mod Menu → Tweakeroo Freecam Plus). They only do
 * something while the free camera is on; otherwise the keys go on to the game and other mods as usual.
 * <ul>
 * <li>Ctrl + L: lock the direction blocks are placed in (see FreecamInteract).</li>
 * <li>Ctrl + Right Shift: your character keeps sneaking until pressed again.</li>
 * </ul>
 */
public final class FreecamHotkeys {
	private FreecamHotkeys() {
	}

	//? if >=1.21.11 <26.1 {
	private static final String PREFIX = "freecamplus.config";
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("tweakeroo_freecam_plus.json");

	// "allow extra keys": the camera may be moving (W held) while you press them
	public static final ConfigHotkey LOCK_DIRECTION = new ConfigHotkey("lockDirection", "LEFT_CONTROL,L", KeybindSettings.PRESS_ALLOWEXTRA, "").apply(PREFIX);
	public static final ConfigHotkey LOCK_SNEAK = new ConfigHotkey("lockSneak", "LEFT_CONTROL,RIGHT_SHIFT", KeybindSettings.PRESS_ALLOWEXTRA, "").apply(PREFIX);
	public static final List<ConfigHotkey> HOTKEYS = List.of(LOCK_DIRECTION, LOCK_SNEAK);

	public static void register() {
		InitializationHandler.getInstance().registerInitializationHandler(() -> {
			ConfigManager.getInstance().registerConfigHandler(FreecamPlus.MOD_ID, Config.INSTANCE);
			Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(FreecamPlus.MOD_ID, "Tweakeroo Freecam Plus", Gui::new));
			InputEventHandler.getKeybindManager().registerKeybindProvider(Provider.INSTANCE);
			LOCK_DIRECTION.getKeybind().setCallback((action, key) -> {
				Minecraft mc = Minecraft.getInstance();
				if (!FreecamInteract.active(mc) || mc.screen != null) return false;
				FreecamInteract.toggleDirectionLock(mc);
				return true;
			});
			LOCK_SNEAK.getKeybind().setCallback((action, key) -> {
				Minecraft mc = Minecraft.getInstance();
				if (!FreecamInteract.active(mc) || mc.screen != null) return false;
				FreecamInteract.toggleSneakLock(mc);
				return true;
			});
		});
	}

	private static final class Provider implements IKeybindProvider {
		static final Provider INSTANCE = new Provider();

		@Override
		public void addKeysToMap(IKeybindManager manager) {
			for (ConfigHotkey hotkey : HOTKEYS) manager.addKeybindToMap(hotkey.getKeybind());
		}

		@Override
		public void addHotkeys(IKeybindManager manager) {
			manager.addHotkeysForCategory("Tweakeroo Freecam Plus", "freecamplus.config.category", HOTKEYS);
		}
	}

	private static final class Config implements IConfigHandler {
		static final Config INSTANCE = new Config();

		@Override
		public void load() {
			if (!Files.isRegularFile(FILE)) return;
			JsonElement element = JsonUtils.parseJsonFile(FILE);
			if (element != null && element.isJsonObject()) ConfigUtils.readConfigBase(element.getAsJsonObject(), "Hotkeys", HOTKEYS);
		}

		@Override
		public void save() {
			JsonObject root = new JsonObject();
			ConfigUtils.writeConfigBase(root, "Hotkeys", HOTKEYS);
			JsonUtils.writeJsonToFile(root, FILE);
		}
	}

	private static final class Gui extends GuiConfigsBase {
		Gui() {
			super(10, 30, FreecamPlus.MOD_ID, null, "freecamplus.config.title");
		}

		@Override
		public List<ConfigOptionWrapper> getConfigs() {
			List<? extends IConfigBase> options = HOTKEYS;
			return ConfigOptionWrapper.createFor(options);
		}
	}
	//?} else {
	/*public static void register() {
	}
	*///?}
}
