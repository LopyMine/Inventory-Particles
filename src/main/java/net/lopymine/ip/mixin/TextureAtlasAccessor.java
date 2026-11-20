package net.lopymine.ip.mixin;

import java.util.Map;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {

	@Accessor("texturesByName")
	Map<Identifier, TextureAtlasSprite> getTexturesByName();

}
