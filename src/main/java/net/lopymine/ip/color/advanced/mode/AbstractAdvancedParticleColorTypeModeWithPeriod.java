package net.lopymine.ip.color.advanced.mode;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;

@Getter
@Setter
public abstract class AbstractAdvancedParticleColorTypeModeWithPeriod extends TickElement implements IAdvancedParticleColorTypeMode {

	protected float speed;
	protected float changeColorTick;

	public AbstractAdvancedParticleColorTypeModeWithPeriod(float speed) {
		this.speed = speed;
	}

	public void updateChangeColorTick() {
		this.changeColorTick = this.ticks + this.speed;
	}

}
