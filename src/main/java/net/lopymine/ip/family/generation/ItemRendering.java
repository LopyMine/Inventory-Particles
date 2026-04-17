package net.lopymine.ip.family.generation;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.mixin.InventoryParticlesImageConsumer;
import net.minecraft.client.*;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ItemRendering {

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;

	@Nullable
	private static Projection GUI_PROJECTION;

	@Nullable
	private static ProjectionMatrixBuffer GUI_PROJECTION_MATRIX_BUFFER;

	@Nullable
	public static volatile TextureTarget TARGET;

	public static void renderFluidIntoImage(BucketItem bucketItem, Consumer<RenderedFluidImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();

		Minecraft.getInstance().execute(() -> {
			GpuTexture colorTexture = renderData.target.getColorTexture();
			GpuTexture depthTexture = renderData.target.getDepthTexture();
			if (colorTexture == null || depthTexture == null) {
				return;
			}

			FluidStateModelSet set = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
			FluidModel model = set.get(bucketItem.getContent().defaultFluidState());
			TextureAtlasSprite sprite = model.stillMaterial().sprite();

			RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
					colorTexture, 0x00000000,
					depthTexture, 1.0F
			);

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, model.tintSource()));
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		Minecraft.getInstance().execute(() -> {
			ItemRendering.renderItemStackToTarget(renderData, itemStack);
			InventoryParticlesImageConsumer specialConsumer = (image) -> {
				consumer.accept(new RenderedItemImage(image));
			};
			Screenshot.takeScreenshot(renderData.target, specialConsumer);
		});
	}

	private static void renderItemStackToTarget(RenderData data, ItemStack itemStack) {
		TextureTarget target = data.target;
		Projection projection = data.projection;
		ProjectionMatrixBuffer buffer = data.buffer;

		GpuTextureView colorView = target.getColorTextureView();
		GpuTextureView depthView = target.getDepthTextureView();
		GpuTexture colorTexture = target.getColorTexture();
		GpuTexture depthTexture = target.getDepthTexture();

		if (colorTexture == null || colorView == null || depthTexture == null || depthView == null) {
			return;
		}


		var oldOutputColor = RenderSystem.outputColorTextureOverride;
		var oldOutputDepth = RenderSystem.outputDepthTextureOverride;
		var oldModelViewMatrix = RenderSystem.getModelViewStack();
		RenderSystem.backupProjectionMatrix();

		RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
				colorTexture, 0x00000000,
				depthTexture, 1.0F
		);
		projection.setupOrtho(-5000.0F, 5000.0F, WIDTH / 2F, HEIGHT / 2F, true);
		RenderSystem.setProjectionMatrix(
				buffer.getBuffer(projection),
				ProjectionType.ORTHOGRAPHIC
		);

		RenderSystem.outputColorTextureOverride = colorView;
		RenderSystem.outputDepthTextureOverride = depthView;
		RenderSystem.getModelViewStack().identity();

		ItemRendering.renderItemStack(itemStack);

		RenderSystem.getModelViewStack().set(oldModelViewMatrix);
		RenderSystem.outputColorTextureOverride = oldOutputColor;
		RenderSystem.outputDepthTextureOverride = oldOutputDepth;
		RenderSystem.restoreProjectionMatrix();
	}

	private static void renderItemStack(ItemStack itemStack) {
		FeatureRenderDispatcher dispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
		BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
				renderState,
				itemStack,
				ItemDisplayContext.GUI,
				null,
				null,
				0
		);


		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.last().pose().mul(new Matrix4f());
		float max = Math.max(WIDTH, HEIGHT) / 2F;

		poseStack.translate(max / 2F, max / 2F, max / 2F);
		poseStack.scale(max, -max, max);

		Minecraft.getInstance().gameRenderer.getLighting().setupFor(Entry.ITEMS_FLAT);

		renderState.submit(poseStack, dispatcher.getSubmitNodeStorage(), 15728880, OverlayTexture.NO_OVERLAY, 0);

		dispatcher.renderAllFeatures();
		dispatcher.endFrame();
		bufferSource.endBatch();
	}

	@NotNull
	private static RenderData checkAndGetTarget() {
		if (TARGET == null) {
			Minecraft.getInstance().execute(() -> {
				TARGET = new TextureTarget("Inventory Particles Block Renderer Target", WIDTH, HEIGHT, true);
				GUI_PROJECTION = new Projection();
				GUI_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("gui");
			});
		}
		TextureTarget target;
		Projection projection;
		ProjectionMatrixBuffer buffer;

		do {
			target = TARGET;
			projection = GUI_PROJECTION;
			buffer = GUI_PROJECTION_MATRIX_BUFFER;
		} while (target == null || projection == null || buffer == null);

		return new RenderData(target, projection, buffer);
	}

	private record RenderData(TextureTarget target, Projection projection, ProjectionMatrixBuffer buffer) {}
}