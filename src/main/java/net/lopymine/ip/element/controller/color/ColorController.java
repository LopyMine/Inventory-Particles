package net.lopymine.ip.element.controller.color;

import lombok.*;
import net.lopymine.ip.element.color.IColorProvider;
import net.lopymine.ip.element.controller.IController;
import net.lopymine.ip.element.base.*;

@Getter
@Setter
public class ColorController<I extends IRepaintable & IRandomizable> implements IController<I> {

	private IColorProvider colorType;

	public ColorController(IColorProvider colorType) {
		this.colorType = colorType;
	}

	@Override
	public void tick(I element) {
		element.setColor(this.colorType.tick(element.getRandom()));
	}
}
