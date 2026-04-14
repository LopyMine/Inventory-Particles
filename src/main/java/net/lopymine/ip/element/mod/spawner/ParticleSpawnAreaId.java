package net.lopymine.ip.element.mod.spawner;

import com.mojang.serialization.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class ParticleSpawnAreaId {

	public static final Identifier STANDARD_SPAWN_AREA = InventoryParticles.id("spawn_areas/standard.png");
	public static final ParticleSpawnAreaId STANDARD_SPAWN_AREA_ID = new ParticleSpawnAreaId(STANDARD_SPAWN_AREA);

	public static final Codec<ParticleSpawnAreaId> CODEC = Codec.STRING.comapFlatMap((s) -> {
		if (s.contains(":")) {
			return Identifier.read(s).map(ParticleSpawnAreaId::new);
		}
		String path = s.endsWith(".png") ? s : s + ".png";
		return DataResult.success(InventoryParticles.id("spawn_areas/" + path)).map(ParticleSpawnAreaId::new);
	}, (id) -> id.getId().toString());

	private Identifier id;

	public ParticleSpawnAreaId(Identifier id) {
		this.id = id;
	}

	private boolean initialized;
	@Nullable
	private ParticleSpawnArea area;

	@Nullable
	public ParticleSpawnArea getArea() {
		if (this.area == null && !this.initialized) {
			this.area = ParticleSpawnArea.readFromTexture(this.id);
			this.initialized = true;
		}
		return this.area;
	}

}
