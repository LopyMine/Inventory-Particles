package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.lopymine.ip.gui.list.AbstractVersionedEntryListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin {

	@WrapWithCondition(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;enableScissor(Lnet/minecraft/client/gui/DrawContext;)V"),
			method = /*? if >=1.21 {*/ "renderWidget" /*?} else {*/ /*"render" *//*?}*/
	)
	private boolean disableScissorEnabling(EntryListWidget<?> instance, DrawContext context) {
		return !(((EntryListWidget<?>) (Object) this) instanceof AbstractVersionedEntryListWidget<?>);
	}

	@WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;disableScissor()V"), method = /*? if >=1.21 {*/ "renderWidget" /*?} else {*/ /*"render" *//*?}*/)
	private boolean disableScissorDisabling(DrawContext instance) {
		return !(((EntryListWidget<?>) (Object) this) instanceof AbstractVersionedEntryListWidget<?>);
	}
}
