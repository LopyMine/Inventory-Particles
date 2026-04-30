package net.lopymine.ip.family.atlas;

import lombok.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class LockableAtlasTexture {

	@NotNull
	private TextureAtlas atlas;
	private boolean locked;
	private Runnable unlockHook;

	public LockableAtlasTexture(@NotNull TextureAtlas atlas) {
		this.atlas = atlas;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		if (!locked && this.unlockHook != null) {
			this.unlockHook.run();
		}
	}
}
