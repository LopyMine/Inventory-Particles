package net.lopymine.ip.config.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.element.*;
import net.lopymine.ip.spawner.ParticleSpawner;
import net.lopymine.ip.utils.CodecUtils;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InvParticleHolder {

	public static final Identifier STANDARD_SPAWN_AREA = InventoryParticles.id("spawn_areas/standard.png");

	public static final Codec<InvParticleHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("item", Items.AIR, Registries.ITEM.getCodec(), InvParticleHolder::getItem),
			option("spawn_area", STANDARD_SPAWN_AREA, Identifier.CODEC, InvParticleHolder::getSpawnArea),
			option("spawn_count", new TwoIntegerRange(), TwoIntegerRange.CODEC, InvParticleHolder::getCountRange),
			option("frequency", new TwoIntegerRange(), TwoIntegerRange.CODEC, InvParticleHolder::getFrequencyRange),
			option("speed_coefficient", 0.0F, Codec.FLOAT, InvParticleHolder::getSpeedCoefficient),
			option("delta_coefficient", 0.0F, Codec.FLOAT, InvParticleHolder::getDeltaCoefficient)
	).apply(instance, InvParticleHolder::new));

	private Item item;
	private Identifier spawnArea;
	private TwoIntegerRange countRange;
	private TwoIntegerRange frequencyRange;
	private float speedCoefficient;
	private float deltaCoefficient;

	public static InvParticleHolder getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public ParticleSpawner create(Function<Cursor, InvParticle> function) {
		return new ParticleSpawner(this.spawnArea, this.countRange, this.frequencyRange, this.speedCoefficient, this.deltaCoefficient, function);
	}

}
