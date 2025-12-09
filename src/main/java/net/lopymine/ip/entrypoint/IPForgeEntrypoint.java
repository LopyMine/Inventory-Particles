package net.lopymine.ip.entrypoint;

//? if forge {
/*import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.MossyLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(InventoryParticles.MOD_ID)
public class IPForgeEntrypoint {

	public IPForgeEntrypoint() {
		InventoryParticles.onInitialize();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> IPForgeClientEntrypoint::onInitializeClient);
	}

}

*///?}
