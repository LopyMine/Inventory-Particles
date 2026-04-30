package net.lopymine.ip.utils.iac;

import com.mojang.blaze3d.platform.NativeImage;
import net.lopymine.ip.utils.NativeImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RenderedFluidImage extends RenderedItemImage {

	@Nullable
	private final ColorGetter colorGetter;

	public RenderedFluidImage(NativeImage image, @Nullable ColorGetter colorGetter) {
		super(image);
		this.colorGetter = colorGetter;
	}

	@Override
	public int getColor(int anotherColor) {
		if (this.colorGetter == null) {
			return anotherColor;
		}

		ClientLevel level = Minecraft.getInstance().level;
		LocalPlayer player = Minecraft.getInstance().player;
		if (level == null || player == null) {
			int tint = this.colorGetter.getFallback(Blocks.AIR.defaultBlockState());
			return NativeImageUtils.applyTint(anotherColor, tint);
		}

		BlockPos pos = player.blockPosition();
		int tint = this.colorGetter.getWorld(level.getBlockState(pos), level, pos);
		return NativeImageUtils.applyTint(anotherColor, tint);
	}

	public interface ColorGetter {

		int getFallback(BlockState state);

		int getWorld(BlockState state, ClientLevel level, BlockPos pos);

	}
}
