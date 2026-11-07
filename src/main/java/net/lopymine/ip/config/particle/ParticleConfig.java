package net.lopymine.ip.config.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.element.mod.InventoryParticle;
import net.lopymine.ip.element.size.*;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class ParticleConfig {

	public static final Codec<DynamicSizesWithInterpolation> DYNAMIC_PARTICLE_SIZE_CODEC = Codec.either(StaticSize.CODEC, DynamicSizesWithInterpolation.CODEC)
			.xmap((either) -> {
				return either.right().orElseGet(() -> either.left().map(DynamicSizesWithInterpolation::fromStatic).orElse(null));
			}, (size) -> {
				StaticSize staticSize = size.toStatic();
				return staticSize == null ? Either.right(size) : Either.left(staticSize);
			});

	public static final Codec<ITexture> TEXTURE_OR_ITEM_CODEC = Identifier.CODEC.xmap((id) -> {
		if (id.getPath().endsWith(".png")) {
			String path = id.getPath();
			String s = path.substring(0, path.length() - 4);
			return new AtlasTexture(id.withPath(s), null);
		}
		return new ItemTexture(new CachedItem(id));
	}, (texture) -> {
		Identifier id = texture.getId();
		return texture instanceof AtlasTexture ? id.withPath(id.getPath() + ".png") : id;
	});

	public static final Codec<ITexture> ADVANCED_TEXTURES_CODEC = Codec.either(TEXTURE_OR_ITEM_CODEC, AtlasTexture.CODEC).xmap((either) -> {
		if (either.right().isPresent()) {
			return either.right().get();
		}
		if (either.left().isPresent()) {
			return either.left().get();
		}
		return null;
	}, (texture) -> {
		if (texture instanceof AtlasTexture elementTexture && elementTexture.getAtlas() != InventoryParticlesAtlasManager.ATLAS_ID) {
			return Either.right(elementTexture);
		}
		return Either.left(texture);
	});

	public static final Codec<ParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("life_time", 0, Codec.INT, ParticleConfig::getLifeTimeTicks),
			option("animation_type", TextureAnimationType.RANDOM, TextureAnimationType.CODEC, ParticleConfig::getAnimationType),
			option("animation_speed", 1.0D, Codec.DOUBLE, ParticleConfig::getAnimationSpeed),
			option("size", DynamicSizesWithInterpolation.STANDARD, DYNAMIC_PARTICLE_SIZE_CODEC, ParticleConfig::getSize),
			option("textures", new ArrayList<>(), ADVANCED_TEXTURES_CODEC, ParticleConfig::getTextures),
			option("holders", new HashSet<>(), ParticleHolder.CODEC, ParticleConfig::getHolders),
			option("physics", ParticlePhysics.getNewInstance(), ParticlePhysics.CODEC, ParticleConfig::getPhysics),
			option("copy_visual_from", Identifier.CODEC, (config) -> Optional.ofNullable(config.getCopyVisualFrom()))
	).apply(instance, (lifeTime, animationType, animationSpeed, size, textures, holders, physics, copyVisualFrom) -> new ParticleConfig(
			lifeTime,
			animationType,
			animationSpeed,
			size,
			textures,
			holders,
			physics,
			copyVisualFrom.orElse(null)
	)));

	public static final List<String> VISUAL_KEYS = List.of("life_time", "animation_type", "animation_speed", "size", "textures", "physics");

	private int lifeTimeTicks;
	private TextureAnimationType animationType;
	private double animationSpeed;
	private DynamicSizesWithInterpolation size;
	private ArrayList<ITexture> textures;
	private HashSet<ParticleHolder> holders;
	private ParticlePhysics physics;
	@Nullable
	private Identifier copyVisualFrom;

	public InventoryParticle createParticle(ParticleSpawnContext context) {
		return InventoryParticle.create(this, context);
	}

	public ParticleConfig copy() {
		return new ParticleConfig(
				this.lifeTimeTicks,
				this.animationType,
				this.animationSpeed,
				this.size,
				new ArrayList<>(this.textures),
				new HashSet<>(this.holders),
				this.physics,
				this.copyVisualFrom
		);
	}

	public void copyVisualFrom(ParticleConfig parent, Collection<String> fields) {
		if (!fields.contains("life_time")) {
			this.lifeTimeTicks = parent.lifeTimeTicks;
		}
		if (!fields.contains("animation_type")) {
			this.animationType = parent.animationType;
		}
		if (!fields.contains("animation_speed")) {
			this.animationSpeed = parent.animationSpeed;
		}
		if (!fields.contains("size")) {
			this.size = parent.size;
		}
		if (!fields.contains("textures")) {
			this.textures = new ArrayList<>(parent.textures);
		}
		if (!fields.contains("physics")) {
			this.physics = parent.physics;
		}
	}

}
