package net.lopymine.ip.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.FamilyParticleData.*;
import net.lopymine.ip.family.atlas.manager.*;
import net.lopymine.ip.family.cache.*;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.utils.NativeImageUtils.NativeImageAndColor;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class TextureGenerationManager {

	public static GeneratedTextures generateWithReplace(RenderedItemImage renderedItemImage, Identifier itemId, Item item, ArrayList<Identifier> textures, TextureGenerationMode textureGenerationMode) {
		ArrayList<ITexture> list = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();

		for (Identifier texture : textures) {
			try {
				CompletableFuture<GenerationResult<ITexture>> future = CompletableFuture.supplyAsync(() -> {
					NativeImage particleImage = NativeImageUtils.loadFromResource(texture.withPrefix("textures/ifamily/"));
					if (particleImage == null) {
						return null;
					}

					Identifier particleId = texture.withPrefix(itemId.getPath() + "/");
					NativeImageAndColor generatedParticle = NativeImageUtils.generateWithReplace(particleImage, renderedItemImage.getImage(), item, textureGenerationMode);

					FamilyParticlesAtlasCacheManager.add(itemId, particleId, generatedParticle.image());

					FamilyParticlesAtlasManager familyManager = FamilyParticlesAtlasManager.getOrCreate(itemId.getNamespace());
					ColoredAtlasTexture directTexture = new ColoredAtlasTexture(
							particleId,
							familyManager.getAtlasId(),
							renderedItemImage::getColor
					);
					return new GenerationResult<>(
							directTexture,
							generatedParticle.averageColor()
					);
				}, Minecraft.getInstance());

				GenerationResult<ITexture> result = future.get();

				if (result == null) {
					throw new NullPointerException("Failed to replace by luminance for \"%s\"".formatted(itemId));
				}

				list.add(result.object);
				colors.add(result.color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new GeneratedTextures(list, colors);
	}

	public record GenerationResult<T>(T object, Integer color) {}

}
