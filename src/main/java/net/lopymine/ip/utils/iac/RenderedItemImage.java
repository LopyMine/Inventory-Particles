package net.lopymine.ip.utils.iac;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class RenderedItemImage {

	private NativeImage image;

	public int getColor(int anotherColor) {
		return anotherColor;
	}

}
