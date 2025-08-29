package net.lopymine.ip.controller.speed;

import java.util.*;
import lombok.*;
import net.lopymine.ip.config.range.FloatRange;
import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.controller.modifier.IControllerModifier;
import net.lopymine.ip.controller.modifier.speed.ISpeedControllerModifier;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.IMovableElement;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class AbstractSpeedController<C extends AbstractSpeedController<C, E>, E extends IMovableElement> implements IController<E> {

	private List<ISpeedControllerModifier<? super C, ? super E>> modifiers = new ArrayList<>();

	@HideInDebugRender
	protected final SpeedConfig config;
	@HideInDebugRender
	protected final Random random;
	protected float lastSpeed;
	protected float speed;

	public AbstractSpeedController(SpeedConfig config, Random random) {
		this.config = config;
		this.speed  = config.getImpulseBidirectional(random);
		this.random = random;
	}

	@Override
	public void tick(E element) {
		this.lastSpeed = this.speed;

		FloatRange maxAcceleration = this.config.getMaxAcceleration();
		float acceleration = this.config.getAccelerationBidirectional(this.random);
		for (ISpeedControllerModifier<? super C, ? super E> m : this.modifiers) {
			acceleration += m.getAcceleration(element);
		}
		boolean canApplyAcceleration = acceleration < 0 ? (this.speed > maxAcceleration.getMin()) : (this.speed < maxAcceleration.getMax());
		if (canApplyAcceleration && acceleration != 0.0F) {
			this.speed += acceleration;
			if (this.speed < maxAcceleration.getMin()) {
				this.speed = maxAcceleration.getMin();
			}
			if (this.speed > maxAcceleration.getMax()) {
				this.speed = maxAcceleration.getMax();
			}
		}

		if (this.speed > 0) {
			float braking = this.config.getBraking();
			for (ISpeedControllerModifier<? super C, ? super E> modifier : this.modifiers) {
				braking += modifier.getBraking(element);
			}
			this.speed -= braking;
			if (this.speed < 0) {
				this.speed = 0;
			}
		}

		if (this.speed < 0) {
			float braking = this.config.getBraking();
			for (ISpeedControllerModifier<? super C, ? super E> modifier : this.modifiers) {
				braking += modifier.getBraking(element);
			}
			this.speed += braking;
			if (this.speed > 0) {
				this.speed = 0;
			}
		}

		float turbulence = this.config.getTurbulence().getRandom(this.random);
		for (ISpeedControllerModifier<? super C, ? super E> modifier : this.modifiers) {
			turbulence += modifier.getTurbulence(element);
		}
		this.speed += turbulence;

		FloatRange max = this.config.getMax();
		if (this.speed < max.getMin()) {
			this.speed = max.getMin();
		}
		if (this.speed > max.getMax()) {
			this.speed = max.getMax();
		}
	}

	@SuppressWarnings("unused")
	public <M extends ISpeedControllerModifier<? super C, ? super E>> void registerModifier(M modifier) {
		this.registerModifier(modifier, false, null);
	}

	public <M extends ISpeedControllerModifier<? super C, ? super E>> void registerModifier(M modifier, boolean impulse, @Nullable E element) {
		this.modifiers.add(modifier);
		if (impulse && element != null) {
			this.speed += modifier.getImpulse(element);
		}
	}
}
