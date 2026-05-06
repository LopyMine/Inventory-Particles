package net.lopymine.ip.mixin.family;

//? if >=1.21.5 {


import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import net.lopymine.ip.utils.mixin.InventoryParticlesImageConsumer;
import net.minecraft.client.Screenshot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screenshot.class)
public class ScreenshotMixin {

	@Nullable
	@Unique
	private static Integer CAPTURED_COLOR;

	@WrapOperation(at = @At(value = "INVOKE", target = "Ljava/nio/ByteBuffer;getInt(I)I", ordinal = 0),
			//? if >=26.1 {
			method = "lambda$takeScreenshot$1"
			//?} else {

			/*//? if fabric {
			method = "method_68156"
			//?} else {
			/^method = "lambda$takeScreenshot$4"
			^///?}

			*///?}
	)
	private static int captureOriginalColor(ByteBuffer instance, int i, Operation<Integer> original) {
		return CAPTURED_COLOR = original.call(instance, i);
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelABGR(III)V", ordinal = 0),
			//? if >=26.1 {
			method = "lambda$takeScreenshot$1"
			//?} else {

			/*//? if fabric {
			method = "method_68156"
			//?} else {
			/^method = "lambda$takeScreenshot$4"
			^///?}

			*///?}
	)
	private static void applyOriginalColorWithAlpha(NativeImage instance, int x, int y, int color, Operation<Void> original, @Local(argsOnly = true) Consumer<NativeImage> consumer) {
		if (consumer instanceof InventoryParticlesImageConsumer && CAPTURED_COLOR != null) {
			@SuppressWarnings("all")
			int i = CAPTURED_COLOR.intValue();
			original.call(instance, x, y, i);
			CAPTURED_COLOR = null;
			return;
		}
		original.call(instance, x, y, color);
	}

}

//?}