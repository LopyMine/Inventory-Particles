package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.lopymine.ip.gui.list.ResourcePackEntriesListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScrollableWidget.class)
public class ScrollableWidgetMixin {

	@ModifyExpressionValue(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/widget/ScrollableWidget;height:I"), method = {"getMaxScrollY", "getScrollbarThumbHeight", "getScrollbarThumbY", "mouseDragged"})
	private int swapHeight(int original) {
		ScrollableWidget widget = ((ScrollableWidget) (Object) this);
		if (!(widget instanceof ResourcePackEntriesListWidget)) {
			return original;
		}
		return original - 4;
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ScrollableWidget;getScrollbarThumbY()I"), method = "drawScrollbar")
	private int beep(ScrollableWidget instance, Operation<Integer> original) {
		Integer o = original.call(instance);
		ScrollableWidget widget = ((ScrollableWidget) (Object) this);
		if (!(widget instanceof ResourcePackEntriesListWidget)) {
			return o;
		}
		return o + 2;
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), method = "drawScrollbar")
	private void beep2(DrawContext instance, RenderPipeline renderPipeline, Identifier identifier, int x, int y, int w, int h, Operation<Void> original) {
		ScrollableWidget widget = ((ScrollableWidget) (Object) this);
		if (!(widget instanceof ResourcePackEntriesListWidget)) {
			original.call(instance, renderPipeline, identifier, x, y, w, h);
			return;
		}
		original.call(instance, renderPipeline, identifier, x, y + 2, w, h - 4);
	}

}
