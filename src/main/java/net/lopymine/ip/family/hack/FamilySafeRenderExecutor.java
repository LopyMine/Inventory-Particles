package net.lopymine.ip.family.hack;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.lopymine.ip.client.InventoryParticlesClient;

public final class FamilySafeRenderExecutor {
    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();

    private FamilySafeRenderExecutor() {}

    public static void submit(Runnable task) {
        QUEUE.add(task);
    }

    public static void run() {
        if (!RenderSystem.isOnRenderThread()) {
            return;
        }

        Runnable task;
        while ((task = QUEUE.poll()) != null) {
            try {
                task.run();
            } catch (Throwable e) {
	            InventoryParticlesClient.LOGGER.error("Failed to execute task:", e);
            }
        }
    }
}