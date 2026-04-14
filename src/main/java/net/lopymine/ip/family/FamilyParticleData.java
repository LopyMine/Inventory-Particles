package net.lopymine.ip.family;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.particle.ParticleHolder;
import net.lopymine.ip.config.range.IntegerRange;
import net.lopymine.ip.element.color.*;
import net.lopymine.ip.element.predicate.nbt.*;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.generation.TextureGenerationManager;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.lopymine.mossylib.utils.CodecUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class FamilyParticleData {

	public static final Identifier NO_PARTICLE_ID = InventoryParticles.id("");

	public static final Codec<FamilyParticleData> ADVANCED_CODEC = create((instance) -> instance.group(
			option("id", NO_PARTICLE_ID, Identifier.CODEC, FamilyParticleData::getId),
			option("textures", new ArrayList<>(), Identifier.CODEC, FamilyParticleData::getTextures),
			option("texture_generation_mode", TextureGenerationMode.ORIGINAL, TextureGenerationMode.CODEC, FamilyParticleData::getTextureGenerationMode),
			option("nbt_conditions_match", NbtNodeMatch.ANY, NbtNodeMatch.CODEC, FamilyParticleData::getMatch),
			option("nbt_conditions", new HashSet<>(), NbtNode.CODEC, FamilyParticleData::getNbtCondition),
			option("spawn_count", new IntegerRange(1, 20), IntegerRange.CODEC, FamilyParticleData::getSpawnCount),
			option("spawn_frequency", new IntegerRange(20, 60), IntegerRange.CODEC, FamilyParticleData::getSpawnFrequency),
			option("color", new StandardColorProvider(), ParticleHolder.STANDARD_AND_ADVANCED_COLOR_TYPE_CODEC, FamilyParticleData::getColorProvider),
			option("speed_coefficient", 1.0D, Codec.DOUBLE, FamilyParticleData::getSpeedCoefficient)
	).apply(instance, FamilyParticleData::new));

	public static final Codec<FamilyParticleData> CODEC = Codec.either(Identifier.CODEC, ADVANCED_CODEC).xmap((either) -> {
		var e = either.mapLeft((id) -> {
			FamilyParticleData created = FamilyParticleData.getNewInstance().get();
			created.setId(id);
			return created;
		});
		return e.right().orElseGet(() -> e.left().orElseThrow());
	}, Either::right);

	private Identifier id;
	private ArrayList<Identifier> textures;
	private TextureGenerationMode textureGenerationMode;

	private NbtNodeMatch match;
	private HashSet<NbtNode> nbtCondition;
	private IntegerRange spawnCount;
	private IntegerRange spawnFrequency;
	private IColorProvider colorProvider;
	private double speedCoefficient;

	public GeneratedTextures getGeneratedTextures(RenderedItemImage renderedItemImage, Identifier itemId, Item item) {
		return switch (this.textureGenerationMode) {
			case ORIGINAL -> new GeneratedTextures(new ArrayList<>(), new ArrayList<>());
			case LUMINANCE_REPLACE -> TextureGenerationManager.luminanceReplace(renderedItemImage, itemId, item, this.textures);
			case COLORIZE_WITH_DOMINANT_COLOR -> TextureGenerationManager.colorizeWithDominantColor(renderedItemImage, this.textures);
		};
	}

	public static Supplier<FamilyParticleData> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public record GeneratedTextures(ArrayList<ITexture> textures, ArrayList<Integer> colors) {}

	public enum TextureGenerationMode implements StringRepresentable {

		ORIGINAL,
		COLORIZE_WITH_DOMINANT_COLOR,
		LUMINANCE_REPLACE;

		public static final Codec<TextureGenerationMode> CODEC = StringRepresentable.fromEnum(TextureGenerationMode::values);

		@NotNull
		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

}
