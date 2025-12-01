package net.lopymine.ip.resourcepack;

import java.util.concurrent.*;
import net.fabricmc.fabric.api.resource.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.texture.IParticleTextureProvider;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;

//? if >=1.21.9 {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
//?}

public class InventoryParticlesClientReloadListener implements /*? if >=1.21.9 {*/ ResourceReloader /*?} else {*/ /*IdentifiableResourceReloadListener *//*?}*/ {

	public static void register() {
		//? if >=1.21.9 {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(getFabricId(), new InventoryParticlesClientReloadListener());
		//?} else {
		/*ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new InventoryParticlesClientReloadListener());
		*///?}
	}

	/*? if <=1.21.8 {*//*@Override*//*?}*/
	public /*? if >=1.21.9 {*/ static /*?}*/ Identifier getFabricId() {
		return InventoryParticles.id("%s-reload-listener".formatted(InventoryParticles.MOD_ID));
	}

	//? if >=1.21.9 {
	@Override
	public CompletableFuture<Void> reload(Store store, Executor prepareExecutor, Synchronizer synchronizer, Executor applyExecutor) {
		return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
			Profiler profiler = Profilers.get();
			profiler.push("listener");
			this.reloadStuff(synchronizer, store.getResourceManager(), prepareExecutor, applyExecutor);
			profiler.pop();
		}, applyExecutor);
	}
	//?} else {
	/*@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, /^? if <=1.21.1 {^/ /^Profiler profiler, Profiler applyProfiler, ^//^?}^/ Executor prepareExecutor, Executor applyExecutor) {
		return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
			//? if >=1.21.2 {
			Profiler profiler = Profilers.get();
			//?}
			profiler.push("listener");
			this.reloadStuff(synchronizer, manager, prepareExecutor, applyExecutor);
			profiler.pop();
		}, applyExecutor);
	}
	*///?}

	public void reloadStuff(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		IParticleTextureProvider.clear();
		InventoryParticlesAtlasManager.getInstance().reload(synchronizer, manager, prepareExecutor, applyExecutor);
		ResourcePackParticleConfigsManager.reload();
	}
}
