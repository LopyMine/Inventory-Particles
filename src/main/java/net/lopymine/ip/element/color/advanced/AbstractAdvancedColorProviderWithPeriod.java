package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;

@Getter
@Setter
public abstract class AbstractAdvancedColorProviderWithPeriod extends TickElement implements IAdvancedColorProvider {

	protected float speed;
	protected float changeColorTick;

	public AbstractAdvancedColorProviderWithPeriod(float speed) {
		this.speed = speed;
	}

	public void updateChangeColorTick() {
		this.changeColorTick = this.ticks + this.speed;
	}

}
