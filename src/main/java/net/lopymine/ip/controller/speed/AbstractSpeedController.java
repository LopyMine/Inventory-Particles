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
	protected final Random random;
	private final float acceleration;
	private final boolean accelerationBidirectional;
	private final FloatRange maxAcceleration;
	private final FloatRange max;
	private final float braking;
	private final FloatRange turbulence;

	protected float lastSpeed;
	protected float speed;

	public AbstractSpeedController(SpeedConfig config, Random random, float impulse) {
		this(random, config.getAcceleration(), config.isAccelerationBidirectional(), config.getMaxAcceleration(), config.getMax(), config.getBraking(), config.getTurbulence(), impulse);
	}

	public AbstractSpeedController(Random random, float acceleration, boolean accelerationBidirectional, FloatRange maxAcceleration, FloatRange max, float braking, FloatRange turbulence, float impulse) {
		this.random                    = random;
		this.acceleration              = acceleration;
		this.accelerationBidirectional = accelerationBidirectional && random.nextBoolean();
		this.maxAcceleration           = this.accelerationBidirectional ? new FloatRange(-maxAcceleration.getMax(), -maxAcceleration.getMin()) : maxAcceleration;
		this.max                       = max;
		this.braking                   = braking;
		this.turbulence                = turbulence;
		this.speed                     = impulse;
	}

	@Override
	public void tick(E element) {
		this.lastSpeed = this.speed;

		float acceleration = this.getAcceleration() * (this.isAccelerationBidirectional() ? -1 : 1);
		for (ISpeedControllerModifier<? super C, ? super E> m : this.modifiers) {
			acceleration += m.getAcceleration(element);
		}
		boolean canApplyAcceleration = acceleration < 0 ? (this.getSpeed() > this.getMaxAcceleration().getMin()) : (this.speed < this.getMaxAcceleration().getMax());
		if (canApplyAcceleration && acceleration != 0.0F) {
			this.speed += acceleration;
			if (this.speed < this.getMaxAcceleration().getMin()) {
				this.speed = this.getMaxAcceleration().getMin();
			}
			if (this.speed > this.getMaxAcceleration().getMax()) {
				this.speed = this.getMaxAcceleration().getMax();
			}
		}

		if (this.speed > 0) {
			float braking = this.getBraking();
			for (ISpeedControllerModifier<? super C, ? super E> modifier : this.modifiers) {
				braking += modifier.getBraking(element);
			}
			this.speed -= braking;
			if (this.speed < 0) {
				this.speed = 0;
			}
		}

		if (this.speed < 0) {
			float braking = this.getBraking();
			for (ISpeedControllerModifier<? super C, ? super E> modifier : this.modifiers) {
				braking += modifier.getBraking(element);
			}
			this.speed += braking;
			if (this.speed > 0) {
				this.speed = 0;
			}
		}

		float turbulence = this.getTurbulence().getRandom(this.random);
		for (ISpeedControllerModifier<? super C, ? super E> modifier : this.modifiers) {
			turbulence += modifier.getTurbulence(element);
		}
		this.speed += turbulence;

		FloatRange max = this.getMax();
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
