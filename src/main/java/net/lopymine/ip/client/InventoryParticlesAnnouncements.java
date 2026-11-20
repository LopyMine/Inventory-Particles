package net.lopymine.ip.client;

import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.family.cache.FamilyParticlesCacheManager;
import net.lopymine.mossylib.announcement.*;

public class InventoryParticlesAnnouncements {

	public static void register() {
		AnnouncementGroup cacheRegeneration = new AnnouncementGroup(
				"cache_regeneration",
				InventoryParticles.text("announcement.cache_regeneration"),
				InventoryParticles.text("announcement.cache_regeneration.option")
		);

		MossyAnnouncements.register(Announcement.builder(InventoryParticles.MOD_ID, "4.0.0/cache_regeneration")
				.group(cacheRegeneration)
				.condition(FamilyParticlesCacheManager::hasModdedItemsCache)
				.option(false, (regenerate) -> {
					if (regenerate) {
						FamilyParticlesCacheManager.deleteSilence();
					}
				})
				.unlockDelay(10)
				.build());

		MossyAnnouncements.register(Announcement.builder(InventoryParticles.MOD_ID, "4.0.0/particles_editor")
				.message(InventoryParticles.text("announcement.particles_editor"))
				.condition(FamilyParticlesCacheManager::hasModdedItemsCache)
				.unlockDelay(5)
				.build());
	}
}
