package net.lopymine.ip.family.atlas.manager;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.Map.Entry;
import net.lopymine.ip.family.atlas.AtlasSprite;
import net.lopymine.ip.family.generation.TextureGenerationManager;
import net.lopymine.ip.utils.MissingSpriteUtils;
import net.minecraft.resources.Identifier;

public class FamilyParticlesAtlasSpriteManager {

	private static final AtlasSprite MISSING_SPRITE = AtlasSprite.of(MissingSpriteUtils.getMissingParticle());

	static {
		MISSING_SPRITE.setClosable(false);
	}

	public static Set<AtlasSprite> createSpritesFromGeneratedTextures() {
		Set<AtlasSprite> set = new HashSet<>();

		for (Entry<Identifier, NativeImage> entry : TextureGenerationManager.getAllGeneratedTextures().entrySet()) {
			AtlasSprite sprite = AtlasSprite.of(entry.getKey(), entry.getValue());
			set.add(sprite);
		}

		set.add(MISSING_SPRITE);

		return set;
	}

}
