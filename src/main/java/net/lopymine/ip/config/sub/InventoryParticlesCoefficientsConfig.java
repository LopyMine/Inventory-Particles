package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import lombok.*;
import static com.mojang.serialization.Codec.DOUBLE;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.*;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesCoefficientsConfig {

	public static final Codec<InventoryParticlesCoefficientsConfig> CODEC = create((instance) -> instance.group(
			option("global_config", ParticleCoefficientConfig.getNewInstance(), ParticleCoefficientConfig.CODEC, InventoryParticlesCoefficientsConfig::getGlobalConfig),
			option("cursor_config", ParticleCoefficientConfig.getNewInstance(), ParticleCoefficientConfig.CODEC, InventoryParticlesCoefficientsConfig::getCursorConfig),
			option("hovered_slot_config", ParticleCoefficientConfig.getNewInstance(), ParticleCoefficientConfig.CODEC, InventoryParticlesCoefficientsConfig::getHoveredSlotConfig),
			option("all_slots_config", ParticleCoefficientConfig.getNewInstance(), ParticleCoefficientConfig.CODEC, InventoryParticlesCoefficientsConfig::getAllSlotsConfig),
			option("gui_action_config", ParticleCoefficientConfig.getNewInstance(), ParticleCoefficientConfig.CODEC, InventoryParticlesCoefficientsConfig::getGuiActionConfig)
	).apply(instance, InventoryParticlesCoefficientsConfig::new));

	private ParticleCoefficientConfig globalConfig;
	private ParticleCoefficientConfig cursorConfig;
	private ParticleCoefficientConfig hoveredSlotConfig;
	private ParticleCoefficientConfig allSlotsConfig;
	private ParticleCoefficientConfig guiActionConfig;

	public static Supplier<InventoryParticlesCoefficientsConfig> getNewInstance() {
		return () -> parseNewInstanceHacky(CODEC);
	}
	
	@Getter
	@Setter
	@AllArgsConstructor
	public static class ParticleCoefficientConfig {

		public static final Codec<ParticleCoefficientConfig> CODEC = create((instance) -> instance.group(
				option("count_coefficient", 1.0D, DOUBLE, ParticleCoefficientConfig::getCountCoefficient),
				option("cooldown_coefficient", 1.0D, DOUBLE, ParticleCoefficientConfig::getCooldownCoefficient)
		).apply(instance, ParticleCoefficientConfig::new));
		
		private double countCoefficient;
		private double cooldownCoefficient;

		public static Supplier<ParticleCoefficientConfig> getNewInstance() {
			return () -> parseNewInstanceHacky(CODEC);
		}
		
	}  	
}
