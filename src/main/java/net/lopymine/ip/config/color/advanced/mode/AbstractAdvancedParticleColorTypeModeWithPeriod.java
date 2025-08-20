package net.lopymine.ip.config.color.advanced.mode;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public abstract class AbstractAdvancedParticleColorTypeModeWithPeriod extends TickElement implements IAdvancedParticleColorTypeMode {

	protected final float speed;
	protected float changeColorTick;

	public AbstractAdvancedParticleColorTypeModeWithPeriod(float speed) {
		this.speed = speed;
	}

	public void updateChangeColorTick() {
		this.changeColorTick = this.ticks + this.speed;
	}

}
