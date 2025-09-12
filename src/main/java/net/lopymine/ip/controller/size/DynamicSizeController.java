package net.lopymine.ip.controller.size;

import java.util.List;
import lombok.*;
import net.lopymine.ip.config.i2o.Integer2DynamicParticleSize;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.particle.DynamicParticleSizeInterpolation;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.element.base.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class DynamicSizeController<E extends IResizableElement & IMovableElement & ITickElement> implements IController<E> {

	private DynamicParticleSizes dynamicParticleSizes;

	private Integer2DynamicParticleSize lastSize;
	private int nextSizeIndex = 0;
	@Nullable
	private Integer2DynamicParticleSize nextSize = null;

	public DynamicSizeController(DynamicParticleSizes dynamicParticleSizes, E element) {
		this.dynamicParticleSizes = dynamicParticleSizes;
		List<Integer2DynamicParticleSize> sizes = dynamicParticleSizes.getSizes();
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
			DynamicParticleSize nextParticleSize = this.nextSize.getObject();

			Integer2DynamicParticleSize lastSize = this.getLastSize(element);
			float progress = (((float) ticks) - lastSize.getIndex()) / (((float) this.nextSize.getIndex()) - lastSize.getIndex());

			DynamicParticleSizeInterpolation interpolation =
					nextParticleSize.getInterpolation() != DynamicParticleSizeInterpolation.NO_INTERPOLATION
					?
					nextParticleSize.getInterpolation()
					:
					this.dynamicParticleSizes.getInterpolation();

			float lastWidth = lastSize.getObject().getWidth();
			float nextWidth = nextParticleSize.getWidth();
			float width = interpolation.getInterpolated(lastWidth, nextWidth, progress);

			float lastHeight = lastSize.getObject().getHeight();
			float nextHeight = nextParticleSize.getHeight();
			float height = interpolation.getInterpolated(lastHeight, nextHeight, progress);

			this.offset(element, width, height);

			element.setWidth(width);
			element.setHeight(height);
			return;
		}
		this.resizeElement(element, this.nextSize);
	}

	private Integer2DynamicParticleSize getLastSize(E element) {
		if (this.lastSize == null) {
			return this.lastSize = new Integer2DynamicParticleSize(0, new DynamicParticleSize(element.getWidth(), element.getHeight()));
		}
		return this.lastSize;
	}

	private void resizeElement(E element, Integer2DynamicParticleSize size) {
		DynamicParticleSize particleSize = size.getObject();
		this.offset(element, particleSize.getWidth(), particleSize.getHeight());
		element.setWidth(particleSize.getWidth());
		element.setHeight(particleSize.getHeight());
		this.lastSize = size;
		if (this.nextSizeIndex + 1 < this.dynamicParticleSizes.getSizes().size()) {
			this.nextSizeIndex++;
			this.nextSize = this.dynamicParticleSizes.getSizes().get(this.nextSizeIndex);
		} else {
			this.nextSize = null;
		}
	}

	private void offset(E element, float nextWidth, float nextHeight) {
		this.offset(element, element.getWidth(), element.getHeight(), nextWidth, nextHeight);
	}

	private void offset(E element, float lastWidth, float lastHeight, float nextWidth, float nextHeight) {
		float offsetX = (nextWidth - lastWidth) / 2F;
		float offsetY = (nextHeight - lastHeight) / 2F;
		element.setX(element.getX() - offsetX);
		element.setY(element.getY() - offsetY);
	}

}
