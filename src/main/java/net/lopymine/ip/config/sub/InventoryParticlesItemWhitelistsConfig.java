package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import java.util.*;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.mossylib.utils.CodecUtils;
import net.lopymine.mossylib.yacl.utils.EnumWithText;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesItemWhitelistsConfig {

	public static final Codec<InventoryParticlesItemWhitelistsConfig> CODEC = create((instance) -> instance.group(
			option("cursor_config", ParticlesItemWhitelistConfig.getNewInstance(), ParticlesItemWhitelistConfig.CODEC, InventoryParticlesItemWhitelistsConfig::getCursorConfig),
			option("hovered_slot_config", ParticlesItemWhitelistConfig.getNewInstance(), ParticlesItemWhitelistConfig.CODEC, InventoryParticlesItemWhitelistsConfig::getHoveredSlotConfig),
			option("all_slots_config", ParticlesItemWhitelistConfig.getNewInstance(), ParticlesItemWhitelistConfig.CODEC, InventoryParticlesItemWhitelistsConfig::getGuiSlotsConfig),
			option("gui_action_config", ParticlesItemWhitelistConfig.getNewInstance(), ParticlesItemWhitelistConfig.CODEC, InventoryParticlesItemWhitelistsConfig::getGuiActionConfig)
	).apply(instance, InventoryParticlesItemWhitelistsConfig::new));

	private ParticlesItemWhitelistConfig cursorConfig;
	private ParticlesItemWhitelistConfig hoveredSlotConfig;
	private ParticlesItemWhitelistConfig guiSlotsConfig;
	private ParticlesItemWhitelistConfig guiActionConfig;

	public static Supplier<InventoryParticlesItemWhitelistsConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public void recompileAll() {
		this.cursorConfig.getCompiledItems(true);
		this.hoveredSlotConfig.getCompiledItems(true);
		this.guiSlotsConfig.getCompiledItems(true);
		this.guiActionConfig.getCompiledItems(true);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class ParticlesItemWhitelistConfig {

		public static final Codec<ParticlesItemWhitelistConfig> CODEC = create((instance) -> instance.group(
				option("mode", Mode.DISABLED, Mode.CODEC, ParticlesItemWhitelistConfig::getMode),
				option("items", "", Codec.STRING, ParticlesItemWhitelistConfig::getItems)
		).apply(instance, ParticlesItemWhitelistConfig::new));

		private Mode mode;
		private String items;
		private int[] compiledItems;

		public ParticlesItemWhitelistConfig(Mode mode, String items) {
			this.mode  = mode;
			this.items = items;
		}

		public static Supplier<ParticlesItemWhitelistConfig> getNewInstance() {
			return () -> CodecUtils.parseNewInstanceHacky(CODEC);
		}

		public int[] getCompiledItems() {
			return this.getCompiledItems(false);
		}

		public int[] getCompiledItems(boolean recompile) {
			if (this.compiledItems != null && !recompile) {
				return this.compiledItems;
			}

			List<Integer> items = new ArrayList<>();

			if (this.items.isEmpty()) {
				return this.compiledItems = new int[0];
			}

			for (String item : this.items.split(" ")) {
				try {
					Identifier itemId = Identifier.parse(item);
					Optional<Reference<Item>> optional = BuiltInRegistries.ITEM.get(itemId);
					if (optional.isEmpty()) {
						InventoryParticlesClient.LOGGER.warn("Invalid item in whitelist: " + item);
						continue;
					}
					int id = BuiltInRegistries.ITEM.getId(optional.get().value());
					if (id == -1) {
						InventoryParticlesClient.LOGGER.warn("Failed to find item in whitelist: " + item);
						continue;
					}
					items.add(id);
				} catch (Exception e) {
					InventoryParticlesClient.LOGGER.warn("Invalid item in whitelist: " + item);
				}
			}

			return this.compiledItems = items.stream().mapToInt(Integer::intValue).toArray();
		}

		public boolean cannotProcess(Item item) {
			if (this.mode == Mode.DISABLED) {
				return false;
			}

			int itemId = BuiltInRegistries.ITEM.getId(item);

			for (int compiledItem : this.getCompiledItems()) {
				if (compiledItem == itemId) {
					return this.mode != Mode.WHITELIST;
				}
			}

			return this.mode != Mode.BLACKLIST;
		}
	}

	public enum Mode implements StringRepresentable, EnumWithText {

		DISABLED,
		WHITELIST,
		BLACKLIST;

		public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

		@Override
		public @NotNull String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}

		@Override
		public Component getText() {
			return InventoryParticles.text("modmenu.option.whitelist_mode." + this.getSerializedName());
		}
	}

}
