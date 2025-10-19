package net.lopymine.ip.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.*;
import java.util.function.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.utils.MissingSpriteUtils;
import net.lopymine.ip.utils.mixin.IAtlasLoaderMixin;
import net.minecraft.client.texture.*;
import net.minecraft.client.texture.atlas.*;
import net.minecraft.client.texture.atlas.AtlasSource.SpriteRegion;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AtlasLoader.class)
public class AtlasLoaderMixin implements IAtlasLoaderMixin {

	@Unique
	private boolean marked;

	//? if >=1.21 {
	/*@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = Shift.AFTER), method = "loadSources", cancellable = true)
	private void swapMissingTexture(ResourceManager resourceManager, CallbackInfoReturnable<List<Function<SpriteOpener, SpriteContents>>> cir, @Local Map<Identifier, SpriteRegion> map) {
		if (!this.marked) {
			return;
		}
		Builder<Function<SpriteOpener, SpriteContents>> builder = ImmutableList.builder();
		builder.add((opener) -> MissingSpriteUtils.getMissingParticle());
		builder.addAll(map.values());
		cir.setReturnValue(builder.build());
	}
	*///?} else {
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = Shift.AFTER), method = "loadSources", cancellable = true)
	private void swapMissingTexture(ResourceManager resourceManager, CallbackInfoReturnable<List<Supplier<SpriteContents>>> cir, @Local Map<Identifier, SpriteRegion> map) {
		if (!this.marked) {
			return;
		}
		Builder<Supplier<SpriteContents>> builder = ImmutableList.builder();
		builder.add(MissingSpriteUtils::getMissingParticle);
		builder.addAll(map.values());
		cir.setReturnValue(builder.build());
	}
	//?}

	@Override
	public void inventoryParticles$mark() {
		this.marked = true;
	}

	@WrapOperation(at = @At(value = "NEW", target = "(Ljava/util/List;)Lnet/minecraft/client/texture/atlas/AtlasLoader;"), method = "of")
	private static AtlasLoader markAtlas(List<AtlasSource> sources, Operation<AtlasLoader> original, @Local(argsOnly = true) Identifier path) {
		AtlasLoader loader = original.call(sources);
		if (InventoryParticlesAtlasManager.FOLDER_ID.equals(path)) {
			((IAtlasLoaderMixin) loader).inventoryParticles$mark();
		}
		return loader;
	}
}
