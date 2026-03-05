package net.lopymine.ip.atlas;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public class OtherAtlasManager {

	public static TextureAtlasSprite getSprite(Identifier sprite, Identifier atlas, TextureAtlasSprite missingSprite) {
		try {
			//? if >=1.21.9 {
			return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlas).getSprite(sprite);
			//?} else {
			/*return Minecraft.getInstance().getTextureAtlas(atlas.withPrefix("textures/atlas/").withSuffix(".png")).apply(sprite);
			*///?}
		} catch (Exception e) {
			return missingSprite;
		}
	}

}
