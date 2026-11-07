package net.lopymine.ip.editor;

import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.element.mod.spawner.ParticleSpawnAreaId;
import net.lopymine.ip.family.FamilyParticlesConfigManager;
import net.lopymine.ip.family.generation.TextureGenerationManager;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;

public class ParticlesPackLayout {

	public static final String FAMILY_TEXTURES_FOLDER_NAME = TextureGenerationManager.FAMILY_TEXTURES_FOLDER_NAME;
	public static final String ATLAS_FILE_NAME = InventoryParticlesAtlasManager.FOLDER_ID.getPath();
	public static final String LEGACY_ATLAS_FILE_NAME = ParticlesConfigsManager.LEGACY_FOLDER_NAME;

	public static final List<LegacyFolder> LEGACY_FOLDERS = List.of(
			new LegacyFolder(ParticlesConfigsManager.LEGACY_FOLDER_NAME, ParticlesConfigsManager.FOLDER_NAME),
			new LegacyFolder(FamilyParticlesConfigManager.LEGACY_FOLDER_NAME, FamilyParticlesConfigManager.FOLDER_NAME),
			new LegacyFolder(ParticleSpawnAreaId.FOLDER_NAME, ParticleSpawnAreaId.FOLDER_NAME),
			new LegacyFolder("textures/" + ParticlesConfigsManager.LEGACY_FOLDER_NAME, "textures/" + ParticlesConfigsManager.FOLDER_NAME),
			new LegacyFolder("textures/ifamily", "textures/" + FAMILY_TEXTURES_FOLDER_NAME)
	);

	public static String assetsFolder(String namespace) {
		return "assets/%s".formatted(namespace);
	}

	public static String modFolder(String namespace) {
		return "%s/%s".formatted(assetsFolder(namespace), InventoryParticles.MOD_ID);
	}

	public static String particlesFolder(String namespace) {
		return "%s/%s".formatted(modFolder(namespace), ParticlesConfigsManager.FOLDER_NAME);
	}

	public static String familiesFolder(String namespace) {
		return "%s/%s".formatted(modFolder(namespace), FamilyParticlesConfigManager.FOLDER_NAME);
	}

	public static String spawnAreasFolder(String namespace) {
		return "%s/%s".formatted(modFolder(namespace), ParticleSpawnAreaId.FOLDER_NAME);
	}

	public static String particleTexturesFolder(String namespace) {
		return texturesFolder(namespace, ParticlesConfigsManager.FOLDER_NAME);
	}

	public static String familyTexturesFolder(String namespace) {
		return texturesFolder(namespace, FAMILY_TEXTURES_FOLDER_NAME);
	}

	private static String texturesFolder(String namespace, String folderName) {
		return "%s/textures/%s/%s".formatted(assetsFolder(namespace), InventoryParticles.MOD_ID, folderName);
	}

	public static boolean isConfigFile(String path) {
		return path.endsWith(".json") || path.endsWith(".json5");
	}

	public record LegacyFolder(String legacyPath, String newPath) {

		public String legacyFolder(String namespace) {
			return "%s/%s".formatted(assetsFolder(namespace), this.legacyPath);
		}

		public String newFolder(String namespace) {
			return this.newPath.startsWith("textures/")
					?
					"%s/textures/%s/%s".formatted(assetsFolder(namespace), InventoryParticles.MOD_ID, this.newPath.substring("textures/".length()))
					:
					"%s/%s".formatted(modFolder(namespace), this.newPath);
		}
	}
}
