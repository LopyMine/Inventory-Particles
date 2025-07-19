package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.function.Function;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.CachedItem;
import net.lopymine.ip.config.color.*;
import net.lopymine.ip.config.range.IntegerRange;
import net.lopymine.ip.element.*;
import net.lopymine.ip.predicate.nbt.*;
import net.lopymine.ip.spawner.ParticleSpawner;
import net.minecraft.util.Identifier;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public class ParticleHolder {

	public static final Identifier STANDARD_SPAWN_AREA = InventoryParticles.id("spawn_areas/standard.png");

	public static final Codec<ParticleHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("item", new CachedItem(), CachedItem.CODEC, ParticleHolder::getItem),
			option("nbt_conditions_match", NbtNodeMatch.ANY, NbtNodeMatch.CODEC, ParticleHolder::getMatch),
			option("nbt_conditions", new HashSet<>(), NbtNode.CODEC, ParticleHolder::getNbtCondition),
			option("spawn_area", STANDARD_SPAWN_AREA, Identifier.CODEC, ParticleHolder::getSpawnArea),
			option("spawn_count", new IntegerRange(), IntegerRange.CODEC, ParticleHolder::getSpawnCount),
			option("spawn_frequency", new IntegerRange(), IntegerRange.CODEC, ParticleHolder::getSpawnFrequency),
			option("color", new StandardParticleColorType(), IParticleColorType.CODEC, ParticleHolder::getColor),
			option("speed_coefficient", 0.0F, Codec.FLOAT, ParticleHolder::getSpeedCoefficient)
	).apply(instance, ParticleHolder::new));

	private CachedItem item;
	private NbtNodeMatch match;
	private HashSet<NbtNode> nbtCondition;
	private Identifier spawnArea;
	private IntegerRange spawnCount;
	private IntegerRange spawnFrequency;
	private IParticleColorType color;
	private float speedCoefficient;

	public ParticleSpawner create(Function<InventoryCursor, InventoryParticle> function) {
		return new ParticleSpawner(this.spawnArea,
				this.spawnCount,
				this.spawnFrequency,
				this.speedCoefficient,
				this.color,
				new NbtParticlePredicate(this.nbtCondition, this.match),
				function
		);
	}

}
