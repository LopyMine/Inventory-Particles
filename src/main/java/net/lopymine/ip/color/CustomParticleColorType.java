package net.lopymine.ip.color;

import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public class CustomParticleColorType implements IParticleColorType {

	private final String original;
	private int color = -1;

	public CustomParticleColorType(String original) {
		this.original = original;
	}

	@Override
	public IParticleColorType copy() {
		return new CustomParticleColorType(this.original);
	}

	@Override
	public int tick(Random random) {
		return this.color;
	}

	@Override
	public void compile(ItemStack stack, Random random) {
		try {
			String color = this.original.substring(1);
			if (color.length() == 6) {
				this.color = 0xFF000000 | Integer.parseInt(color, 16);
			} else if (color.length() == 8) {
				this.color = (int) Long.parseLong(color, 16);
			}
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to parse custom color from \"{}\"! Reason:", this.original, e);
		}
	}

	@Override
	public String asString() {
		return "custom";
	}

	@Override
	public String toString() {
		return this.getString(this.color);
	}
}
