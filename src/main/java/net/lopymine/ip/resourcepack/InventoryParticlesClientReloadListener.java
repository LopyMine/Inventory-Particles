package net.lopymine.ip.resourcepack;

import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.texture.IParticleTextureProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.*;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.*;
import net.minecraft.util.profiling.*;

//? if fabric && <=1.21.8 {
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
*///?}

public class InventoryParticlesClientReloadListener implements /*? if >=1.21.9 || forge || neoforge {*/ PreparableReloadListener /*?} else {*/ /*IdentifiableResourceReloadListener *//*?}*/ {

	//? if fabric {
	//? if <=1.21.8 {
	/*@Override
	*///?}
	public /*? if >=1.21.9 {*/ static /*?}*/ ResourceLocation getFabricId() {
		return getId();
	}
	//?}

	public static ResourceLocation getId() {
		return InventoryParticles.id("%s-reload-listener".formatted(InventoryParticles.MOD_ID));
	}

	//? if >=1.21.9 {
	@Override
	public CompletableFuture<Void> reload(SharedState store, Executor prepareExecutor, PreparationBarrier synchronizer, Executor applyExecutor) {
		return synchronizer.wait(Unit.INSTANCE).thenRunAsync(() -> {
			ProfilerFiller profiler = Profiler.get();
			profiler.push("listener");
			this.reloadStuff(synchronizer, store.resourceManager(), prepareExecutor, applyExecutor);
			profiler.pop();
		}, applyExecutor);
	}
	//?} else {
	/*@Override
	public CompletableFuture<Void> reload(PreparationBarrier synchronizer, ResourceManager manager, /^? if <=1.21.1 {^/ /^ProfilerFiller profiler, ProfilerFiller applyProfiler, ^//^?}^/ Executor prepareExecutor, Executor applyExecutor) {
		return synchronizer.wait(Unit.INSTANCE).thenRunAsync(() -> {
			//? if >=1.21.2 {
			ProfilerFiller profiler = Profiler.get();
			//?}
			profiler.push("listener");
			this.reloadStuff(synchronizer, manager, prepareExecutor, applyExecutor);
			profiler.pop();
		}, applyExecutor);
	}
	*///?}

	public void reloadStuff(PreparationBarrier synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		IParticleTextureProvider.clear();
		InventoryParticlesAtlasManager.getInstance().reload(synchronizer, manager, prepareExecutor, applyExecutor);
		ResourcePackParticleConfigsManager.reload();
	}
}
