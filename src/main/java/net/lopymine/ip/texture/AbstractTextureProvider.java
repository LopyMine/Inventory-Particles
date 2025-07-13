package net.lopymine.ip.texture;

import java.util.*;
import lombok.*;
import net.lopymine.ip.element.TickElement;
import net.minecraft.util.Identifier;

@Setter
@Getter
@AllArgsConstructor
public abstract class AbstractTextureProvider extends TickElement implements ITextureProvider {

	protected List<Identifier> textures;
	protected int animationSpeed;
	protected int lifeTime;
	protected boolean dead;

	public AbstractTextureProvider(List<Identifier> textures, int animationSpeed, int lifeTime) {
		this(textures, animationSpeed, lifeTime, false);
	}

	protected void markDead() {
		this.dead = true;
	}
}
