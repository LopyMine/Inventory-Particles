package net.lopymine.ip.controller.size;

import java.util.List;
import net.lopymine.ip.config.misc.Integer2ParticleSize;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.element.base.*;
import org.jetbrains.annotations.Nullable;

public class DynamicSizeController<E extends IResizableElement & ITickElement> implements IController<E> {

	private final DynamicParticleSize dynamicParticleSize;

	private int nextSizeIndex = 0;
	@Nullable
	private Integer2ParticleSize nextSize = null;
	@Nullable
	private Integer2ParticleSize lastSize = null;

	public DynamicSizeController(DynamicParticleSize dynamicParticleSize, E element) {
		this.dynamicParticleSize = dynamicParticleSize;
		List<Integer2ParticleSize> sizes = dynamicParticleSize.getSizes();
		if (!sizes.isEmpty()) {
			Integer2ParticleSize size = sizes.get(0);
			if (size.getIndex() <= 0) {
				this.resizeElement(element, size);
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
			ParticleSize nextParticleSize = this.nextSize.getObject();
			float progress = (((float) ticks) - (this.lastSize != null ? this.lastSize.getIndex() : 0)) / (((float) this.nextSize.getIndex()) - (this.lastSize != null ? this.lastSize.getIndex() : 0));
			ParticleSize lastParticleSize = this.lastSize != null ? this.lastSize.getObject() : null;
			element.setWidth(this.dynamicParticleSize.getInterpolation().getInterpolated(lastParticleSize != null ? lastParticleSize.getWidth() : element.getWidth(), nextParticleSize.getWidth(), progress));
			element.setHeight(this.dynamicParticleSize.getInterpolation().getInterpolated(lastParticleSize != null ? lastParticleSize.getHeight() : element.getHeight(), nextParticleSize.getHeight(), progress));
			return;
		}
		this.resizeElement(element, this.nextSize);
	}

	private void resizeElement(E element, Integer2ParticleSize size) {
		ParticleSize particleSize = size.getObject();
		element.setWidth(particleSize.getWidth());
		element.setHeight(particleSize.getHeight());
		this.lastSize = size;
		if (this.nextSizeIndex + 1 < this.dynamicParticleSize.getSizes().size()) {
			this.nextSizeIndex++;
			this.nextSize = this.dynamicParticleSize.getSizes().get(this.nextSizeIndex);
		} else {
			this.nextSize = null;
		}
	}

}
