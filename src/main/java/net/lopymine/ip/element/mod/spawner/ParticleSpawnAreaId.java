package net.lopymine.ip.element.mod.spawner;

import com.mojang.serialization.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class ParticleSpawnAreaId {

	public static final String FOLDER_NAME = "spawn_areas";
	public static final Identifier STANDARD_SPAWN_AREA = spawnAreaId(InventoryParticles.MOD_ID, "standard.png");
	public static final ParticleSpawnAreaId STANDARD_SPAWN_AREA_ID = new ParticleSpawnAreaId(STANDARD_SPAWN_AREA);

	public static final Codec<ParticleSpawnAreaId> CODEC = Codec.STRING.comapFlatMap((s) -> {
		String path = s.endsWith(".png") ? s : s + ".png";
		if (s.contains(":")) {
			return Identifier.read(path).map((id) -> new ParticleSpawnAreaId(spawnAreaId(id.getNamespace(), id.getPath()), id));
		}
		return DataResult.success(new ParticleSpawnAreaId(spawnAreaId(InventoryParticles.MOD_ID, path), InventoryParticles.id("%s/%s".formatted(FOLDER_NAME, path))));
	}, ParticleSpawnAreaId::asString);

	private static Identifier spawnAreaId(String namespace, String path) {
		return InventoryParticles.id(namespace, "%s/%s/%s".formatted(InventoryParticles.MOD_ID, FOLDER_NAME, path));
	}

	private Identifier id;
	@Nullable
	private final Identifier legacyId;

	public ParticleSpawnAreaId(Identifier id) {
		this(id, null);
	}

	public ParticleSpawnAreaId(Identifier id, @Nullable Identifier legacyId) {
		this.id = id;
		this.legacyId = legacyId;
	}

	private boolean initialized;
	@Nullable
	private ParticleSpawnArea area;

	@Nullable
	public ParticleSpawnArea getArea() {
		if (this.area == null && !this.initialized) {
			this.area = ParticleSpawnArea.readFromTexture(this.id);
			if ((this.area == null || this.area.isEmpty()) && this.legacyId != null) {
				ParticleSpawnArea legacyArea = ParticleSpawnArea.readFromTexture(this.legacyId);
				if (legacyArea != null && !legacyArea.isEmpty()) {
					this.area = legacyArea;
				}
			}
			this.initialized = true;
		}
		return this.area;
	}

	public String asString() {
		String prefix = "%s/%s/".formatted(InventoryParticles.MOD_ID, FOLDER_NAME);
		String path = this.id.getPath().startsWith(prefix) ? this.id.getPath().substring(prefix.length()) : this.id.getPath();
		return this.id.getNamespace().equals(InventoryParticles.MOD_ID) ? path : "%s:%s".formatted(this.id.getNamespace(), path);
	}

}
