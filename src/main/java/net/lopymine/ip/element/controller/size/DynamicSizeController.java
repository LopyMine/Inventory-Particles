package net.lopymine.ip.element.controller.size;

import java.util.List;
import lombok.*;
import net.lopymine.ip.config.i2o.Integer2DynamicParticleSize;
import net.lopymine.ip.element.size.*;
import net.lopymine.ip.element.controller.IController;
import net.lopymine.ip.element.base.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class DynamicSizeController<E extends IResizableElement & IMovableElement & ITickElement> implements IController<E> {

	private DynamicSizesWithInterpolation dynamicSizesWithInterpolation;

	private Integer2DynamicParticleSize lastSize;
	private int nextSizeIndex = 0;
	@Nullable
	private Integer2DynamicParticleSize nextSize = null;

	public DynamicSizeController(DynamicSizesWithInterpolation dynamicSizesWithInterpolation, E element) {
		this.dynamicSizesWithInterpolation = dynamicSizesWithInterpolation;
		List<Integer2DynamicParticleSize> sizes = dynamicSizesWithInterpolation.getSizes();
		if (!sizes.isEmpty()) {
			Integer2DynamicParticleSize size = sizes.get(0);
			if (size.getIndex() <= 0) {
				this.resizeElement(element, size);
				element.setLastWidth(element.getWidth());
				element.setLastHeight(element.getHeight());
			} else {
				this.nextSize      = size;
				this.nextSizeIndex = 0;
			}
		}
	}

	@Override
	public void tick(E element) {
		int ticks = element.getTicks();
		if (this.nextSize == null) {
			return;
		}
		if (ticks < this.nextSize.getIndex()) {
			DynamicSize nextParticleSize = this.nextSize.getObject();

			Integer2DynamicParticleSize lastSize = this.getLastSize(element);
			double progress = (((double) ticks) - lastSize.getIndex()) / (((double) this.nextSize.getIndex()) - lastSize.getIndex());

			DynamicSizeInterpolation interpolation =
					nextParticleSize.getInterpolation() != DynamicSizeInterpolation.NO_INTERPOLATION
					?
					nextParticleSize.getInterpolation()
					:
					this.dynamicSizesWithInterpolation.getInterpolation();

			double lastWidth = lastSize.getObject().getWidth();
			double nextWidth = nextParticleSize.getWidth();
			double width = interpolation.getInterpolated(lastWidth, nextWidth, progress);

			double lastHeight = lastSize.getObject().getHeight();
			double nextHeight = nextParticleSize.getHeight();
			double height = interpolation.getInterpolated(lastHeight, nextHeight, progress);

			this.offset(element, width, height);

			element.setWidth(width);
			element.setHeight(height);
			return;
		}
		this.resizeElement(element, this.nextSize);
	}

	private Integer2DynamicParticleSize getLastSize(E element) {
		if (this.lastSize == null) {
			return this.lastSize = new Integer2DynamicParticleSize(0, new DynamicSize(element.getWidth(), element.getHeight()));
		}
		return this.lastSize;
	}

	private void resizeElement(E element, Integer2DynamicParticleSize size) {
		DynamicSize particleSize = size.getObject();
		this.offset(element, particleSize.getWidth(), particleSize.getHeight());
		element.setWidth(particleSize.getWidth());
		element.setHeight(particleSize.getHeight());
		this.lastSize = size;
		if (this.nextSizeIndex + 1 < this.dynamicSizesWithInterpolation.getSizes().size()) {
			this.nextSizeIndex++;
			this.nextSize = this.dynamicSizesWithInterpolation.getSizes().get(this.nextSizeIndex);
		} else {
			this.nextSize = null;
		}
	}

	private void offset(E element, double nextWidth, double nextHeight) {
		this.offset(element, element.getWidth(), element.getHeight(), nextWidth, nextHeight);
	}

	private void offset(E element, double lastWidth, double lastHeight, double nextWidth, double nextHeight) {
		double offsetX = (nextWidth - lastWidth) / 2F;
		double offsetY = (nextHeight - lastHeight) / 2F;
		element.setX(element.getX() - offsetX);
		element.setY(element.getY() - offsetY);
	}

}
