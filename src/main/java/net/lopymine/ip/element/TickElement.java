package net.lopymine.ip.element;

import lombok.*;

@Getter
@Setter
public class TickElement implements ITickElement{

	protected int ticks;

	public void tick() {
		this.ticks++;
		if (this.ticks == Integer.MAX_VALUE) {
			this.ticks = 0;
		}
	}

}
