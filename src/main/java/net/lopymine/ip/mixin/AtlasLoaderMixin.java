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
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=1.21 {
@Mixin(SpriteSourceList.class)
//?} else {
/*@Mixin(SpriteResourceLoader.class)
*///?}
public class AtlasLoaderMixin implements IAtlasLoaderMixin {

	@Unique
	private boolean inventoryParticles$marked;

	//? if >=1.21.11 {
	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					shift = Shift.AFTER
			),
			//? if neoforge {
			/*method = "list(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/List;",
			*///?} else {
			method = "list",
			//?}
			cancellable = true
	)
	private void swapMissingTexture(CallbackInfoReturnable<List<SpriteSource.Loader>> cir, @Local Map<Identifier, SpriteSource.DiscardableLoader> map) {
		if (!this.inventoryParticles$marked) {
			return;
		}
		Builder<SpriteSource.Loader> builder = ImmutableList.builder();
		builder.add((opener) -> MissingSpriteUtils.getMissingParticle());
		builder.addAll(map.values());
		cir.setReturnValue(builder.build());
	}
	//?} elif >=1.21 {
	/*@Inject(
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					shift = Shift.AFTER
			),
			//? if neoforge && >=1.21.10 {
			/^method = "list(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/List;",
			^///?} else {
			method = "list",
			//?}
			cancellable = true
	)
	private void swapMissingTexture(CallbackInfoReturnable<List<Function<SpriteResourceLoader, SpriteContents>>> cir, @Local Map<Identifier, SpriteSource.SpriteSupplier> map) {
		if (!this.inventoryParticles$marked) {
			return;
		}
		Builder<Function<SpriteResourceLoader, SpriteContents>> builder = ImmutableList.builder();
		builder.add((opener) -> MissingSpriteUtils.getMissingParticle());
		builder.addAll(map.values());
		cir.setReturnValue(builder.build());
	}
	*///?} else {
	/*@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = Shift.AFTER), method = "list", cancellable = true)
	private void swapMissingTexture(ResourceManager resourceManager, CallbackInfoReturnable<List<Supplier<SpriteContents>>> cir, @Local Map<Identifier, SpriteSource.SpriteSupplier> map) {
		if (!this.inventoryParticles$marked) {
			return;
		}
		Builder<Supplier<SpriteContents>> builder = ImmutableList.builder();
		builder.add(MissingSpriteUtils::getMissingParticle);
		builder.addAll(map.values());
		cir.setReturnValue(builder.build());
	}
	*///?}

	@Override
	public void inventoryParticles$mark() {
		this.inventoryParticles$marked = true;
	}

	//? if >=1.21 {
	@WrapOperation(at = @At(value = "NEW", target = "(Ljava/util/List;)Lnet/minecraft/client/renderer/texture/atlas/SpriteSourceList;"), method = "load")
	private static SpriteSourceList markAtlas(List<SpriteSourceList> sources, Operation<SpriteSourceList> original, @Local(argsOnly = true) Identifier path) {
		SpriteSourceList loader = original.call(sources);
		if (InventoryParticlesAtlasManager.FOLDER_ID.equals(path)) {
			((IAtlasLoaderMixin) loader).inventoryParticles$mark();
		}
		return loader;
	}
	//?} else {
	/*@WrapOperation(at = @At(value = "NEW", target = "(Ljava/util/List;)Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;"), method = "load")
	private static SpriteResourceLoader markAtlas(List<SpriteResourceLoader> sources, Operation<SpriteResourceLoader> original, @Local(argsOnly = true) Identifier path) {
		SpriteResourceLoader loader = original.call(sources);
		if (InventoryParticlesAtlasManager.FOLDER_ID.equals(path)) {
			((IAtlasLoaderMixin) loader).inventoryParticles$mark();
		}
		return loader;
	}
	*///?}
}
