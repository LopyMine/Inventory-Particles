package net.lopymine.ip.family.atlas.manager;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.Map.Entry;
import net.lopymine.ip.family.atlas.AtlasSprite;
import net.lopymine.ip.family.cache.FamilyParticlesAtlasCacheManager;
import net.lopymine.ip.family.generation.TextureGenerationManager;
import net.lopymine.ip.utils.MissingSpriteUtils;
import net.minecraft.resources.Identifier;

public class FamilyParticlesAtlasSpriteManager {

	private static final AtlasSprite MISSING_SPRITE = AtlasSprite.of(MissingSpriteUtils.getMissingParticle());

	static {
		MISSING_SPRITE.setClosable(false);
	}

	public static Map<String, Set<AtlasSprite>> createSpritesFromGeneratedTextures() {
		Map<String, Set<AtlasSprite>> map = new HashMap<>();

		for (Entry<String, Map<Identifier, NativeImage>> e : FamilyParticlesAtlasCacheManager.getNamespaceTextures().entrySet()) {
			String atlasId = e.getKey();
			Set<AtlasSprite> set = new HashSet<>();
			for (Entry<Identifier, NativeImage> entry : e.getValue().entrySet()) {
				AtlasSprite sprite = AtlasSprite.of(entry.getKey(), entry.getValue());
				set.add(sprite);
			}
			set.add(MISSING_SPRITE);
			map.put(atlasId, set);
		}

		return map;
	}

}
