package net.lopymine.ip.config.color;

import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

@Setter
@Getter
public class CustomParticleColorType implements IParticleColorType {

	private int color = -1;

	public static CustomParticleColorType parse(String s) {
		CustomParticleColorType type = new CustomParticleColorType();
		try {
			String color = s.substring(1);
			if (color.length() == 6) {
				type.setColor(0xFF000000 | Integer.parseInt(color, 16));
			} else if (color.length() == 8) {
				type.setColor((int) Long.parseLong(color, 16));
			}
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to parse custom color from \"{}\"! Reason:", s, e);
		}
		return type;
	}

	@Override
	public int getColor(ItemStack stack, Random random) {
		return this.getColor();
	}

	@Override
	public String getAsString() {
		return "";
	}
}
