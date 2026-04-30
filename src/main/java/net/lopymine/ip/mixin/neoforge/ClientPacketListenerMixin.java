package net.lopymine.ip.mixin.neoforge;

//? if neoforge {
/*import net.lopymine.ip.entrypoint.IPNeoForgeClientEntrypoint.LevelJoinEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(method = {"handleLogin"}, at = {@At("RETURN")})
	private void handleServerPlayReady(ClientboundLoginPacket packet, CallbackInfo ci) {
		NeoForge.EVENT_BUS.post(new LevelJoinEvent());
	}

}

*///?}
