package net.lopymine.ip.element.color;

import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

@Getter
@Setter
public class CustomColorProvider implements IColorProvider {

	private final String original;
	private int color = -1;

	public CustomColorProvider(String original) {
		this.original = original;
	}

	@Override
	public IColorProvider copy() {
		return new CustomColorProvider(this.original);
	}

	@Override
	public int tick(RandomSource random) {
		return this.color;
	}

	@Override
	public void compile(ItemStack stack, RandomSource random) {
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
