package net.lopymine.ip.mixin.forge;

//? if forge {
/*import net.lopymine.ip.entrypoint.IPForgeClientEntrypoint.LevelJoinEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(method = {"handleLogin"}, at = {@At("RETURN")})
	private void handleServerPlayReady(ClientboundLoginPacket packet, CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new LevelJoinEvent());
	}

}
*///?}