package net.lopymine.ip.utils;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;

public class MissingSpriteUtils {

	public static SpriteContents getMissingParticle() {
		NativeImage image = MissingSprite.createImage(6, 6);
		NativeImage nativeImage = new NativeImage(8, 8, false);
		image.copyRect(nativeImage, 0, 0, 1, 1, 6, 6, false,false);
		image.close();

		return new SpriteContents(MissingSprite.getMissingSpriteId(), new SpriteDimensions(8, 8), nativeImage /*? if >=1.21 && <=1.21.8 {*//*,ResourceMetadata.NONE *//*?} elif <=1.21.8 {*/ ,AnimationResourceMetadata.EMPTY /*?}*/);
	}
}
