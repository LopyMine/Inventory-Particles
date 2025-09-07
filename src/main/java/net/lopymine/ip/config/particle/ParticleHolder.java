package net.lopymine.ip.config.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.function.Function;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.color.*;
import net.lopymine.ip.color.advanced.AdvancedParticleColorType;
import net.lopymine.ip.color.advanced.mode.AdvancedParticleColorTypeRandomStaticMode;
import net.lopymine.ip.config.CachedItem;
import net.lopymine.ip.config.range.IntegerRange;
import net.lopymine.ip.element.*;
import net.lopymine.ip.predicate.nbt.*;
import net.lopymine.ip.spawner.ParticleSpawner;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.minecraft.util.Identifier;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public class ParticleHolder {

	public static final Identifier STANDARD_SPAWN_AREA = InventoryParticles.id("spawn_areas/standard.png");

	public static final Codec<IParticleColorType> STANDARD_AND_LIST_COLOR_TYPE_CODEC = Codec.either(IParticleColorType.CODEC, IParticleColorType.CODEC.listOf())
			.xmap((either) -> {
				Optional<List<IParticleColorType>> right = either.right();
				if (right.isPresent()) {
					return new AdvancedParticleColorType(new AdvancedParticleColorTypeRandomStaticMode(), right.get(), 0);
				}
				Optional<IParticleColorType> left = either.left();
				return left.orElse(null);
			}, (type) -> {
				if (type instanceof AdvancedParticleColorType advancedType) {
					return Either.right(advancedType.getValues());
				}
				return Either.left(type);
			});

	public static final Codec<IParticleColorType> STANDARD_AND_ADVANCED_COLOR_TYPE_CODEC = Codec.either(AdvancedParticleColorType.CODEC, STANDARD_AND_LIST_COLOR_TYPE_CODEC)
			.xmap((either) -> {
				Optional<IParticleColorType> right = either.right();
				if (right.isPresent()) {
					return right.get();
				}
				Optional<AdvancedParticleColorType> left = either.left();
				return left.orElse(null);
			}, (type) -> {
				if (type instanceof AdvancedParticleColorType advancedType) {
					return Either.left(advancedType);
				}
				return Either.right(type);
			});

	public static final Codec<Identifier> SPAWN_AREA_CODEC = Codec.STRING.comapFlatMap((s) -> {
		if (s.contains(":")) {
			return Identifier.validate(s);
		}
		String path = s.endsWith(".png") ? s : s + ".png";
		return DataResult.success(InventoryParticles.id("spawn_areas/" + path));
	}, Identifier::toString);

	public static final Codec<ParticleHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("item", new CachedItem(), CachedItem.CODEC, ParticleHolder::getItem),
			option("nbt_conditions_match", NbtNodeMatch.ANY, NbtNodeMatch.CODEC, ParticleHolder::getMatch),
			option("nbt_conditions", new HashSet<>(), NbtNode.CODEC, ParticleHolder::getNbtCondition),
			option("spawn_area", STANDARD_SPAWN_AREA, SPAWN_AREA_CODEC, ParticleHolder::getSpawnArea),
			option("spawn_count", new IntegerRange(), IntegerRange.CODEC, ParticleHolder::getSpawnCount),
			option("spawn_frequency", new IntegerRange(), IntegerRange.CODEC, ParticleHolder::getSpawnFrequency),
			option("color", new StandardParticleColorType(), STANDARD_AND_ADVANCED_COLOR_TYPE_CODEC, ParticleHolder::getColor),
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

	public ParticleSpawner create(Function<ParticleSpawnContext, InventoryParticle> function) {
		return new ParticleSpawner(this.spawnArea,
				this.spawnCount,
				this.spawnFrequency,
				this.speedCoefficient,
				this.color,
				new NbtParticleSpawnPredicate(this.nbtCondition, this.match),
				function
		);
	}

}
