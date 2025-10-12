package net.lopymine.ip.mixin;

import net.lopymine.ip.utils.mixin.IDrawContextMixin;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrawContext.class)
public class DrawContextMixin implements IDrawContextMixin {

	@Unique
	private boolean marked;

	@Inject(at = @At("RETURN"), method = "scissorContains", cancellable = true)
	private void cancelCheckForWidgets(int x, int y, CallbackInfoReturnable<Boolean> cir) {
		if (this.marked) {
			cir.setReturnValue(true);
		}
	}

	@Override
	public void inventoryParticles$mark() {
		this.marked = true;
	}

	@Override
	public void inventoryParticles$unmark() {
		this.marked = true;
	}
}
