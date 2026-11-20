package net.lopymine.ip.atlas;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.atlas.LockableAtlasTexture;
import net.lopymine.ip.family.atlas.manager.FamilyParticlesAtlasManager;
import net.lopymine.ip.mixin.TextureAtlasAccessor;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class MissingSpritesReporter {

	private static final AtomicBoolean DUMPED = new AtomicBoolean();
	private static volatile boolean active;

	public static void reset() {
		active = false;
	}

	public static void activate() {
		DUMPED.set(false);
		active = true;
	}

	public static boolean isMissing(@Nullable TextureAtlasSprite sprite) {
		return sprite != null && MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
	}

	public static boolean isDebug() {
		return active && InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled();
	}

	public static void reportMissing(AtlasTexture texture) {
		if (!active) {
			return;
		}
		dumpAtlasesOnce();
		InventoryParticlesClient.LOGGER.warn("Failed to render a particle with texture \"{}\" from atlas \"{}\". See the Inventory Particles dynamic atlases dump.", texture.getSpriteNotNull(), texture.getAtlas());
	}

	public static void reportTexture(ITexture texture) {
		if (!active) {
			return;
		}
		dumpAtlasesOnce();
		if (texture instanceof AtlasTexture atlasTexture) {
			InventoryParticlesClient.LOGGER.info("Spawned a particle with texture \"{}\" from atlas \"{}\"", atlasTexture.getSpriteNotNull(), atlasTexture.getAtlas());
			return;
		}
		InventoryParticlesClient.LOGGER.info("Spawned a particle with {} \"{}\"", texture.getClass().getSimpleName(), texture.getId());
	}

	private static void dumpAtlasesOnce() {
		if (DUMPED.compareAndSet(false, true)) {
			dumpAtlases();
		}
	}

	private static void dumpAtlases() {
		StringBuilder builder = new StringBuilder("============= INVENTORY PARTICLES DYNAMIC ATLASES DUMP =============\n");
		appendAtlas(builder, InventoryParticlesAtlasManager.ATLAS_ID, InventoryParticlesAtlasManager.getInstance().getAtlas());
		for (String key : new TreeSet<>(FamilyParticlesAtlasManager.keys())) {
			FamilyParticlesAtlasManager manager = FamilyParticlesAtlasManager.get(key);
			if (manager == null) {
				continue;
			}
			LockableAtlasTexture atlasTexture = manager.getAtlasTexture();
			appendAtlas(builder, manager.getAtlasId(), atlasTexture == null ? null : atlasTexture.getAtlas());
		}
		builder.append("====================================================================");
		InventoryParticlesClient.LOGGER.info("\n{}", builder);
	}

	private static void appendAtlas(StringBuilder builder, Identifier atlasId, @Nullable TextureAtlas atlas) {
		builder.append(atlasId).append(' ');
		if (atlas == null) {
			builder.append("<not stitched>\n");
			return;
		}
		List<String> sprites = ((TextureAtlasAccessor) atlas).getTexturesByName()
				.keySet()
				.stream()
				.map((id) -> "\"" + id + "\"")
				.sorted()
				.toList();
		builder.append('[').append(String.join(", ", sprites)).append("]\n");
	}

}
