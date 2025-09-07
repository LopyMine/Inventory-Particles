package net.lopymine.ip.resourcepack;

import java.util.concurrent.*;
import net.fabricmc.fabric.api.resource.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.texture.IParticleTextureProvider;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;

public class InventoryParticlesClientReloadListener implements IdentifiableResourceReloadListener {

	public static void register() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new InventoryParticlesClientReloadListener());
	}

	@Override
	public Identifier getFabricId() {
		return InventoryParticles.id("%s-reload-listener".formatted(InventoryParticles.MOD_ID));
	}

	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, /*? if <=1.21.1 {*/ /*Profiler profiler, Profiler applyProfiler, *//*?}*/ Executor prepareExecutor, Executor applyExecutor) {
		return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
			//? if >=1.21.2 {
			Profiler profiler = Profilers.get();
			//?}
			profiler.push("listener");
			this.reloadStuff(synchronizer, manager, prepareExecutor, applyExecutor);
			profiler.pop();
		}, applyExecutor);
	}

	public void reloadStuff(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		IParticleTextureProvider.clear();
		InventoryParticlesAtlasManager.getInstance().reload(synchronizer, manager, prepareExecutor, applyExecutor);
		ResourcePackParticleConfigsManager.reload();
	}
}
