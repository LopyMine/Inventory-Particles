package net.lopymine.ip.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.metadata.animation.*;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.resources.ResourceLocation;

public class MissingSpriteUtils {

	public static SpriteContents getMissingParticle() {
		NativeImage image = MissingTextureAtlasSprite.generateMissingImage(6, 6);
		NativeImage nativeImage = new NativeImage(8, 8, false);
		image.copyRect(nativeImage, 0, 0, 1, 1, 6, 6, false,false);
		image.close();

		return new SpriteContents(MissingTextureAtlasSprite.getLocation(), new FrameSize(8, 8), nativeImage /*? if >=1.21 && <=1.21.8 {*//*,ResourceMetadata.EMPTY *//*?} elif <=1.21.8 {*/ /*, AnimationMetadataSection.EMPTY *//*?}*/);
	}
}
