package net.lopymine.ip.controller.color;

import lombok.*;
import net.lopymine.ip.color.IParticleColorType;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.element.base.*;

@Getter
@Setter
public class ColorController<I extends IRepaintable & IRandomizable> implements IController<I> {

	private IParticleColorType colorType;

	public ColorController(IParticleColorType colorType) {
		this.colorType = colorType;
	}

	@Override
	public void tick(I element) {
		element.setColor(this.colorType.tick(element.getRandom()));
	}
}
