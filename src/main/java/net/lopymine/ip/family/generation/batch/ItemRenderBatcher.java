package net.lopymine.ip.family.generation.batch;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.family.generation.ItemRendering;
import net.lopymine.ip.utils.iac.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemRenderBatcher {

	private static final int WAIT_STEP_MS = 1;
	private static final int MAX_RENDER_TIME = 10000;

	public static RenderedItemImages render(List<ItemRenderRequest> requests) {
		RenderedItemImages images = new RenderedItemImages();
		if (requests.isEmpty() || Minecraft.getInstance().level == null) {
			return images;
		}

		List<ItemRenderRequest> fluidRequests = new ArrayList<>();
		List<ItemRenderRequest> itemRequests  = new ArrayList<>();

		for (ItemRenderRequest request : requests) {
			List<ItemRenderRequest> list = request.isFluid() ? fluidRequests : itemRequests;
			list.add(request);
		}

		InventoryParticlesClient.LOGGER.info("Batching item images...");
		long a = System.currentTimeMillis();
		renderFluids(fluidRequests, images);
		long b = System.currentTimeMillis();

		InventoryParticlesClient.LOGGER.info("Fluids took {} seconds. Amount: {}", (b - a) / 1000D, fluidRequests.size());

		long c = System.currentTimeMillis();
		renderItems(itemRequests, images);
		long d = System.currentTimeMillis();

		InventoryParticlesClient.LOGGER.info("Items took {} seconds. Amount: {}", (d - c) / 1000D, itemRequests.size());

		return images;
	}

	private static void renderFluids(List<ItemRenderRequest> requests, RenderedItemImages images) {
		if (requests.isEmpty()) {
			return;
		}

		List<BucketItem> buckets = new ArrayList<>(requests.size());
		for (ItemRenderRequest request : requests) {
			buckets.add(request.bucket());
		}

		BatchResult<List<RenderedFluidImage>> result = new BatchResult<>();
		ItemRendering.renderFluidsIntoImages(buckets, result::complete);

		List<RenderedFluidImage> rendered = awaitAngGet(result, "%s fluids".formatted(buckets.size()));
		if (rendered == null) {
			return;
		}

		for (int i = 0; i < requests.size() && i < rendered.size(); i++) {
			RenderedFluidImage image = rendered.get(i);
			if (image != null) {
				images.putFluid(requests.get(i).item(), image);
			}
		}
	}

	private static void renderItems(List<ItemRenderRequest> requests, RenderedItemImages images) {
		for (int from = 0; from < requests.size(); from += ItemRendering.MAX_ITEMS_PER_ATLAS) {
			int to = Math.min(from + ItemRendering.MAX_ITEMS_PER_ATLAS, requests.size());
			renderPage(requests.subList(from, to), images);
		}
	}

	private static void renderPage(List<ItemRenderRequest> page, RenderedItemImages images) {
		int columns = Math.min((int) Math.ceil(Math.sqrt(page.size())), ItemRendering.MAX_ATLAS_CELLS);
		int rows    = (page.size() + columns - 1) / columns;

		List<ItemStack> itemStacks = new ArrayList<>(page.size());
		for (ItemRenderRequest request : page) {
			itemStacks.add(request.item().getDefaultInstance());
		}

		BatchResult<NativeImage> result = new BatchResult<>();
		ItemRendering.renderItemsIntoAtlas(itemStacks, columns, rows, result::complete);

		NativeImage atlas = awaitAngGet(result, "%s items".formatted(page.size()));
		if (atlas == null) {
			return;
		}

		try {
			for (int i = 0; i < page.size(); i++) {
				images.putItem(page.get(i).item(), new RenderedItemImage(sliceCell(atlas, i, columns)));
			}
		} finally {
			atlas.close();
		}
	}

	private static NativeImage sliceCell(NativeImage atlas, int index, int columns) {
		int size = ItemRendering.CELL_SIZE;

		NativeImage cell = new NativeImage(size, size, true);
		atlas.copyRect(
				cell,
				(index % columns) * size,
				(index / columns) * size,
				0,
				0,
				size,
				size,
				false,
				false
		);
		return cell;
	}

	@Nullable
	private static <T> T awaitAngGet(BatchResult<T> result, String description) {
		int waited = 0;

		while (!result.isReady() && waited < MAX_RENDER_TIME) {
			try {
				Thread.sleep(WAIT_STEP_MS);
				waited += WAIT_STEP_MS;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}

		if (!result.isReady()) {
			InventoryParticles.LOGGER.error("Skipping rendering of {} because it took too long!", description);
			return null;
		}

		return result.getValue();
	}
}
