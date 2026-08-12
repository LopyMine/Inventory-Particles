package net.lopymine.ip.mixin;

//? if >=26.2 {

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import java.util.List;
import java.util.function.Consumer;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.family.atlas.manager.FamilyParticlesAtlasManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {

	@Shadow
	@Final
	private Identifier location;

	@WrapOperation(at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0), method = "clearTextureData")
	private void preventFamilySpritesFromClosingBecauseOfReusing(List<TextureAtlasSprite> instance, Consumer<?> consumer, Operation<Void> original) {
		if (this.location.getNamespace().equals(InventoryParticles.MOD_ID) && FamilyParticlesAtlasManager.keys().contains(this.location.getPath())) {
			return;
		}
		original.call(instance, consumer);
	}

}
//?}
