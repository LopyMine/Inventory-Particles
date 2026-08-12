package net.lopymine.ip.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.FamilyParticleData.*;
import net.lopymine.ip.family.atlas.manager.*;
import net.lopymine.ip.family.cache.*;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.utils.NativeImageUtils.*;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class TextureGenerationManager {

	private static final Map<Identifier, Optional<NativeImage>> TEMPLATES = new ConcurrentHashMap<>();

	public static GeneratedTextures generateWithReplace(RenderedItemImage renderedItemImage, Identifier itemId, Item item, ArrayList<Identifier> textures, TextureGenerationMode textureGenerationMode) {
		if (textures.isEmpty()) {
			return new GeneratedTextures(new ArrayList<>(), new ArrayList<>());
		}

		ArrayList<ITexture> list = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();

		SourceColors sourceColors = NativeImageUtils.clusterColors(renderedItemImage.getImage(), textureGenerationMode);

		for (Identifier texture : textures) {
			try {
				NativeImage particleImage = getTemplate(texture);
				if (particleImage == null) {
					throw new NullPointerException("Failed to load particle template \"%s\" for \"%s\"".formatted(texture, itemId));
				}

				Identifier particleId = texture.withPrefix(itemId.getPath() + "/");

				NativeImageAndColor generatedParticle = NativeImageUtils.generateWithReplace(particleImage, sourceColors, item);

				FamilyParticlesAtlasCacheManager.add(itemId, particleId, generatedParticle.image());

				FamilyParticlesAtlasManager familyManager = FamilyParticlesAtlasManager.getOrCreate(itemId.getNamespace());
				ColoredAtlasTexture directTexture = new ColoredAtlasTexture(
						particleId,
						familyManager.getAtlasId(),
						renderedItemImage::getColor
				);

				list.add(directTexture);
				colors.add(generatedParticle.averageColor());
			} catch (Exception e) {
				InventoryParticles.LOGGER.error("Failed to generate particle texture for \"{}\":", itemId, e);
			}
		}

		return new GeneratedTextures(list, colors);
	}

	@Nullable
	private static NativeImage getTemplate(Identifier texture) {
		Optional<NativeImage> cached = TEMPLATES.get(texture);
		if (cached != null) {
			return cached.orElse(null);
		}

		NativeImage image = NativeImageUtils.loadFromResource(texture.withPrefix("textures/ifamily/"));
		TEMPLATES.put(texture, Optional.ofNullable(image));
		return image;
	}

	public static void clearTemplates() {
		for (Optional<NativeImage> template : TEMPLATES.values()) {
			template.ifPresent(NativeImage::close);
		}
		TEMPLATES.clear();
	}

}
