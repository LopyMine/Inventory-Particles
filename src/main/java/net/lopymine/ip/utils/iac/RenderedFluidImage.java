package net.lopymine.ip.utils.iac;

import com.mojang.blaze3d.platform.NativeImage;
import net.lopymine.ip.utils.NativeImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class RenderedFluidImage extends RenderedItemImage {

	@Nullable
	private final BlockTintSource source;

	public RenderedFluidImage(NativeImage image, @Nullable BlockTintSource source) {
		super(image);
		this.source = source;
	}

	@Override
	public int getColor(int anotherColor) {
		if (this.source == null) {
			return anotherColor;
		}

		ClientLevel level = Minecraft.getInstance().level;
		LocalPlayer player = Minecraft.getInstance().player;
		if (level == null || player == null) {
			int tint = this.source.color(Blocks.AIR.defaultBlockState());
			return NativeImageUtils.applyTint(anotherColor, tint);
		}

		BlockPos pos = player.blockPosition();
		int tint = this.source.colorInWorld(level.getBlockState(pos), level, pos);
		return NativeImageUtils.applyTint(anotherColor, tint);
	}
}
