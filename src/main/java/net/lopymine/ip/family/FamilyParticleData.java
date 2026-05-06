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
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.*;
import static com.mojang.serialization.Codec.BOOL;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.*;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class FamilyParticleData {

	public static final Identifier NO_PARTICLE_ID = InventoryParticles.id("");

	public static final Codec<FamilyParticleData> ADVANCED_CODEC = create((instance) -> instance.group(
			option("id", NO_PARTICLE_ID, Identifier.CODEC, FamilyParticleData::getId),
			option("textures", new ArrayList<>(), Identifier.CODEC, FamilyParticleData::getTextures),
			option("texture_generation_mode", TextureGenerationMode.getNewInstance(), TextureGenerationMode.CODEC, FamilyParticleData::getTextureGenerationMode),
			option("texture_extract_mode", TextureExtractMode.ITEM, TextureExtractMode.CODEC, FamilyParticleData::getTextureExtractMode),
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
	private TextureExtractMode textureExtractMode;

	private NbtNodeMatch match;
	private HashSet<NbtNode> nbtCondition;
	private IntegerRange spawnCount;
	private IntegerRange spawnFrequency;
	private IColorProvider colorProvider;
	private double speedCoefficient;

	@NotNull
	public GeneratedTextures getGeneratedTextures(RenderedItemImage renderedItemImage, Identifier itemId, Item item) {
		if (this.textures.isEmpty()) {
			return new GeneratedTextures(new ArrayList<>(), new ArrayList<>());
		}
		if (this.textureGenerationMode.isDisabled()) {
			return new GeneratedTextures(new ArrayList<>(), new ArrayList<>());
		}
		return TextureGenerationManager.generateWithReplace(renderedItemImage, itemId, item, this.textures, this.textureGenerationMode);
	}

	public static Supplier<FamilyParticleData> getNewInstance() {
		return () -> parseNewInstanceHacky(CODEC);
	}

	public record GeneratedTextures(ArrayList<ITexture> textures, @Nullable ArrayList<Integer> colors) {}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class TextureGenerationMode {

		public static final Codec<TextureGenerationMode> CODEC = create((instance) -> instance.group(
				option("luminance", false, Codec.BOOL, TextureGenerationMode::isLuminance),
				option("saturation", false, Codec.BOOL, TextureGenerationMode::isSaturation),
				option("frequency", false, Codec.BOOL, TextureGenerationMode::isFrequency)
		).apply(instance, TextureGenerationMode::new));

		private boolean luminance;
		private boolean saturation;
		private boolean frequency;

		public boolean isDisabled() {
			return !this.luminance && !this.saturation && !this.frequency;
		}

		public static Supplier<TextureGenerationMode> getNewInstance() {
			return () -> parseNewInstanceHacky(CODEC);
		}

	}

	public enum TextureExtractMode implements StringRepresentable {

		ITEM,
		FLUID;

		public static final Codec<TextureExtractMode> CODEC = StringRepresentable.fromEnum(TextureExtractMode::values);

		@NotNull
		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

}
