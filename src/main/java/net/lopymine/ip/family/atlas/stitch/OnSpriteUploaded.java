package net.lopymine.ip.family.atlas.stitch;

import net.lopymine.ip.family.atlas.AtlasSprite;

public interface OnSpriteUploaded {

	void onUploaded(AtlasSprite sprite);

	default OnSpriteUploaded then(OnSpriteUploaded then) {
		return (sprite) -> {
			this.onUploaded(sprite);
			then.onUploaded(sprite);
		};
	}
}
