package net.lopymine.ip.family.atlas.stitch;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.lopymine.ip.InventoryParticles;
import org.jetbrains.annotations.Nullable;

public class StitchHooksManager {

	private final Queue<OnAtlasStitched> stitchHooks = new ConcurrentLinkedQueue<>();

	public void addHook(@Nullable OnAtlasStitched onAtlasStitched) {
		if (onAtlasStitched != null) {
			this.stitchHooks.add(onAtlasStitched);
		}
	}

	public void runAllHooks(boolean successful) {
		OnAtlasStitched currentHook;
		while ((currentHook = this.stitchHooks.poll()) != null) {
			try {
				currentHook.onStitch(successful);
			} catch (Exception e) {
				InventoryParticles.LOGGER.warn("Unexpected error on stitch hooking:", e);
			}
		}
	}

}
