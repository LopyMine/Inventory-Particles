package net.lopymine.ip.family.generation;

//? if >=26.1 {

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.iac.RenderedFluidImage.ColorGetter;
import net.lopymine.ip.utils.mixin.InventoryParticlesImageConsumer;
import net.minecraft.client.*;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
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

		FamilySafeRenderExecutor.submit(() -> {
			GpuTexture colorTexture = renderData.target.getColorTexture();
			GpuTexture depthTexture = renderData.target.getDepthTexture();
			if (colorTexture == null || depthTexture == null) {
				consumer.accept(null);
				return;
			}

			FluidStateModelSet set = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
			FluidModel model = set.get(bucketItem.getContent().defaultFluidState());
			TextureAtlasSprite sprite = model.stillMaterial().sprite();

			if (sprite.contents().name().getPath().equals("missingno")) {
				consumer.accept(null);
				return;
			}

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {

				@Nullable
				private final BlockTintSource source = model.tintSource();

				@Override
				public int getFallback(BlockState state) {
					if (this.source == null) {
						return -1;
					}
					return this.source.color(state);
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					if (this.source == null) {
						return -1;
					}
					return this.source.colorInWorld(state, level, pos);
				}
			}));
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		FamilySafeRenderExecutor.submit(() -> {
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
			FamilySafeRenderExecutor.submit(() -> {
				TARGET                       = new TextureTarget("Inventory Particles Block Renderer Target", WIDTH, HEIGHT, true);
				GUI_PROJECTION               = new Projection();
				GUI_PROJECTION_MATRIX_BUFFER = new ProjectionMatrixBuffer("gui");
			});
		}
		TextureTarget target;
		Projection projection;
		ProjectionMatrixBuffer buffer;

		do {
			target     = TARGET;
			projection = GUI_PROJECTION;
			buffer     = GUI_PROJECTION_MATRIX_BUFFER;
		} while (target == null || projection == null || buffer == null);

		return new RenderData(target, projection, buffer);
	}

	private record RenderData(TextureTarget target, Projection projection, ProjectionMatrixBuffer buffer) {

	}
}
//?} elif >=1.21.10 {

/*import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.iac.RenderedFluidImage.ColorGetter;
import net.lopymine.ip.utils.mixin.InventoryParticlesImageConsumer;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

//? if fabric {
import net.fabricmc.fabric.api.client.render.fluid.v1.*;
//?} else {
/^import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
^///?}

public class ItemRendering {

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;

	@Nullable
	private static CachedOrthoProjectionMatrixBuffer BUFFER;

	@Nullable
	public static volatile TextureTarget TARGET;

	public static void renderFluidIntoImage(BucketItem bucketItem, Consumer<RenderedFluidImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();

		FamilySafeRenderExecutor.submit(() -> {
			GpuTexture colorTexture = renderData.target.getColorTexture();
			GpuTexture depthTexture = renderData.target.getDepthTexture();
			if (colorTexture == null || depthTexture == null) {
				consumer.accept(null);
				return;
			}

			FluidState fluidState = bucketItem.getContent().defaultFluidState();
			//? if fabric {
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(bucketItem.getContent());
			if (handler == null) {
				consumer.accept(null);
				return;
			}

			TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, fluidState);
			TextureAtlasSprite sprite = fluidSprites[0];

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return handler.getFluidColor(null, null, fluidState);
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return handler.getFluidColor(level, pos, fluidState);
				}
			}));
			//?} else {

			/^IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluidState);
			TextureAtlas atlas;
			try {
				atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
			} catch (Exception e) {
				consumer.accept(null);
				throw new RuntimeException(e);
			}
			Identifier stillTexture = extensions.getStillTexture();
			if (stillTexture == null) { // can be null!! Ignore warning
				consumer.accept(null);
				return;
			}

			TextureAtlasSprite sprite = atlas.getTextures().get(stillTexture);
			if (sprite == null) {
				consumer.accept(null);
				return;
			}

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().getOriginalImage().copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return extensions.getTintColor();
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return extensions.getTintColor(fluidState, level, pos);
				}
			}));
			^///?}
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		FamilySafeRenderExecutor.submit(() -> {
			ItemRendering.renderItemStackToTarget(renderData, itemStack);
			InventoryParticlesImageConsumer specialConsumer = (image) -> {
				consumer.accept(new RenderedItemImage(image));
			};
			Screenshot.takeScreenshot(renderData.target, specialConsumer);
		});
	}

	private static void renderItemStackToTarget(RenderData data, ItemStack itemStack) {
		TextureTarget target = data.target;
		CachedOrthoProjectionMatrixBuffer buffer = data.buffer;

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
		RenderSystem.setProjectionMatrix(
				buffer.getBuffer(WIDTH / 2F, HEIGHT / 2F),
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
			FamilySafeRenderExecutor.submit(() -> {
				TARGET = new TextureTarget("Inventory Particles Block Renderer Target", WIDTH, HEIGHT, true);
				BUFFER = new CachedOrthoProjectionMatrixBuffer(
						"item_rendering_inventory_particles",
						-5000.0F,
						5000.0F,
						true
				);
			});
		}
		TextureTarget target;
		CachedOrthoProjectionMatrixBuffer buffer;

		do {
			target = TARGET;
			buffer = BUFFER;
		} while (target == null || buffer == null);

		return new RenderData(target, buffer);
	}

	private record RenderData(TextureTarget target, CachedOrthoProjectionMatrixBuffer buffer) {}
}
*///?} elif >=1.21.8 {

/*import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.*;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.iac.RenderedFluidImage.ColorGetter;
import net.lopymine.ip.utils.mixin.InventoryParticlesImageConsumer;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ItemRendering {

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;

	@Nullable
	private static CachedOrthoProjectionMatrixBuffer BUFFER;

	@Nullable
	public static volatile TextureTarget TARGET;

	public static void renderFluidIntoImage(BucketItem bucketItem, Consumer<RenderedFluidImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();

		FamilySafeRenderExecutor.submit(() -> {
			GpuTexture colorTexture = renderData.target.getColorTexture();
			GpuTexture depthTexture = renderData.target.getDepthTexture();
			if (colorTexture == null || depthTexture == null) {
				consumer.accept(null);
				return;
			}

			Fluid content = bucketItem.content;
			FluidState fluidState = content.defaultFluidState();
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(content);
			if (handler == null) {
				consumer.accept(null);
				return;
			}

			TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, fluidState);
			RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
					colorTexture, 0x00000000,
					depthTexture, 1.0F
			);

			TextureAtlasSprite sprite = fluidSprites[0];

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return handler.getFluidColor(null, null, fluidState);
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return handler.getFluidColor(level, pos, fluidState);
				}
			}));
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		FamilySafeRenderExecutor.submit(() -> {
			ItemRendering.renderItemStackToTarget(renderData, itemStack);
			InventoryParticlesImageConsumer specialConsumer = (image) -> {
				consumer.accept(new RenderedItemImage(image));
			};
			Screenshot.takeScreenshot(renderData.target, specialConsumer);
		});
	}

	private static void renderItemStackToTarget(RenderData data, ItemStack itemStack) {
		TextureTarget target = data.target;
		CachedOrthoProjectionMatrixBuffer buffer = data.buffer;

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
		RenderSystem.setProjectionMatrix(
				buffer.getBuffer(WIDTH / 2F, HEIGHT / 2F),
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

		renderState.render(poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);

		bufferSource.endBatch();
	}

	@NotNull
	private static RenderData checkAndGetTarget() {
		if (TARGET == null) {
			FamilySafeRenderExecutor.submit(() -> {
				TARGET = new TextureTarget("Inventory Particles Block Renderer Target", WIDTH, HEIGHT, true);
				BUFFER = new CachedOrthoProjectionMatrixBuffer(
						"item_rendering_inventory_particles",
						-5000.0F,
						5000.0F,
						true
				);
			});
		}
		TextureTarget target;
		CachedOrthoProjectionMatrixBuffer buffer;

		do {
			target = TARGET;
			buffer = BUFFER;
		} while (target == null || buffer == null);

		return new RenderData(target, buffer);
	}

	private record RenderData(TextureTarget target, CachedOrthoProjectionMatrixBuffer buffer) {}
}
*///?} elif >=1.21.5 {

/*import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.*;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.iac.RenderedFluidImage.ColorGetter;
import net.lopymine.ip.utils.mixin.InventoryParticlesImageConsumer;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ItemRendering {

	public static boolean SWAP_TARGET = false;

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;

	private static final ItemStackRenderState RENDER_STATE = new ItemStackRenderState();

	@Nullable
	public static volatile TextureTarget TARGET;

	public static void renderFluidIntoImage(BucketItem bucketItem, Consumer<RenderedFluidImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();

		FamilySafeRenderExecutor.submit(() -> {
			GpuTexture colorTexture = renderData.target.getColorTexture();
			GpuTexture depthTexture = renderData.target.getDepthTexture();
			if (colorTexture == null || depthTexture == null) {
				consumer.accept(null);
				return;
			}

			Fluid content = bucketItem.content;
			FluidState fluidState = content.defaultFluidState();
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(content);
			if (handler == null) {
				consumer.accept(null);
				return;
			}

			TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, fluidState);
			RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
					colorTexture, 0x00000000,
					depthTexture, 1.0F
			);

			TextureAtlasSprite sprite = fluidSprites[0];

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return handler.getFluidColor(null, null, fluidState);
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return handler.getFluidColor(level, pos, fluidState);
				}
			}));
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		FamilySafeRenderExecutor.submit(() -> {
			ItemRendering.renderItemStackToTarget(renderData, itemStack);
			InventoryParticlesImageConsumer specialConsumer = (image) -> {
				consumer.accept(new RenderedItemImage(image));
			};
			Screenshot.takeScreenshot(renderData.target, specialConsumer);
		});
	}

	private static void renderItemStackToTarget(RenderData data, ItemStack itemStack) {
		TextureTarget target = data.target;

		GpuTexture colorTexture = target.getColorTexture();
		GpuTexture depthTexture = target.getDepthTexture();

		if (colorTexture == null || depthTexture == null) {
			return;
		}

		var oldModelViewMatrix = RenderSystem.getModelViewStack();
		RenderSystem.backupProjectionMatrix();
		RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
				colorTexture, 0x00000000,
				depthTexture, 1.0F
		);

		Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, WIDTH / 2F, HEIGHT / 2F, 0.0F, -5000F, 5000F);
		RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.ORTHOGRAPHIC);
		RenderSystem.getModelViewStack().identity();

		ItemRendering.renderItemStack(itemStack);

		RenderSystem.getModelViewStack().set(oldModelViewMatrix);
		RenderSystem.restoreProjectionMatrix();
	}

	private static void renderItemStack(ItemStack itemStack) {
		BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(RENDER_STATE, itemStack, ItemDisplayContext.GUI, null, null, 0);

		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.last().pose().mul(new Matrix4f());
		float max = Math.max(WIDTH, HEIGHT) / 2F;

		poseStack.translate(max / 2F, max / 2F, max / 2F);
		poseStack.scale(max, -max, max);

		bufferSource.endBatch();
		Lighting.setupForFlatItems();
		SWAP_TARGET = true;

		RENDER_STATE.render(poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);

		bufferSource.endBatch();
		Lighting.setupFor3DItems();
		SWAP_TARGET = false;
	}

	@NotNull
	private static RenderData checkAndGetTarget() {
		if (TARGET == null) {
			FamilySafeRenderExecutor.submit(() -> {
				TARGET = new TextureTarget("Inventory Particles Block Renderer Target", WIDTH, HEIGHT, true);
			});
		}
		TextureTarget target;

		do {
			target = TARGET;
		} while (target == null);

		return new RenderData(target);
	}

	private record RenderData(TextureTarget target) {}
}
*///?} elif >=1.21.4 {
/*import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.*;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.iac.RenderedFluidImage.ColorGetter;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ItemRendering {

	public static boolean SWAP_TARGET = false;

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;

	private static final ItemStackRenderState RENDER_STATE = new ItemStackRenderState();

	@Nullable
	public static volatile TextureTarget TARGET;

	public static void renderFluidIntoImage(BucketItem bucketItem, Consumer<RenderedFluidImage> consumer) {
		RenderSystem.recordRenderCall(() -> {
			Fluid content = bucketItem.content;
			FluidState fluidState = content.defaultFluidState();
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(content);
			if (handler == null) {
				consumer.accept(null);
				return;
			}

			TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, fluidState);

			TextureAtlasSprite sprite = fluidSprites[0];

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return handler.getFluidColor(null, null, fluidState);
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return handler.getFluidColor(level, pos, fluidState);
				}
			}));
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		RenderSystem.recordRenderCall(() -> {
			ItemRendering.renderItemStackToTarget(renderData, itemStack);

			TextureTarget target = renderData.target;
			NativeImage nativeImage = new NativeImage(target.width, target.height, false);
			RenderSystem.bindTexture(target.getColorTextureId());
			nativeImage.downloadTexture(0, false);
			nativeImage.flipY();

			consumer.accept(new RenderedItemImage(nativeImage));
		});
	}

	private static void renderItemStackToTarget(RenderData data, ItemStack itemStack) {
		TextureTarget target = data.target;

		var oldModelViewMatrix = RenderSystem.getModelViewStack();
		RenderSystem.backupProjectionMatrix();

		target.bindWrite(true);
		GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 0.0F);
		int a = 16384;
		GlStateManager._clearDepth(1.0);
		a |= 256;
		GlStateManager._clear(a);

		Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, WIDTH / 2F, HEIGHT / 2F, 0.0F, -5000F, 5000F);
		RenderSystem.setProjectionMatrix(matrix4f, ProjectionType.ORTHOGRAPHIC);
		RenderSystem.getModelViewStack().identity();

		ItemRendering.renderItemStack(itemStack, target);

		RenderSystem.getModelViewStack().set(oldModelViewMatrix);
		RenderSystem.restoreProjectionMatrix();
	}

	private static void renderItemStack(ItemStack itemStack, TextureTarget target) {
		BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(RENDER_STATE, itemStack, ItemDisplayContext.GUI, false, null, null, 0);

		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.last().pose().mul(new Matrix4f());
		float max = Math.max(WIDTH, HEIGHT) / 2F;

		poseStack.translate(max / 2F, max / 2F, max / 2F);
		poseStack.scale(max, -max, max);

		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

		bufferSource.endBatch();
		Lighting.setupForFlatItems();
		SWAP_TARGET = true;
		target.bindWrite(true);

		RENDER_STATE.render(poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);

		bufferSource.endBatch();
		Lighting.setupFor3DItems();
		SWAP_TARGET = false;

		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
	}

	@NotNull
	private static RenderData checkAndGetTarget() {
		if (TARGET == null) {
			RenderSystem.recordRenderCall(() -> {
				TARGET = new TextureTarget(WIDTH, HEIGHT, true);
			});
		}
		TextureTarget target;

		do {
			target = TARGET;
		} while (target == null);

		return new RenderData(target);
	}

	private record RenderData(TextureTarget target) {}
}
*///?} else {
/*import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.function.Consumer;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import net.lopymine.ip.utils.iac.*;
import net.lopymine.ip.utils.iac.RenderedFluidImage.ColorGetter;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

//? if fabric {

import net.fabricmc.fabric.api.client.render.fluid.v1.*;

//?} elif neoforge {

/^import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;

^///?} else {
/^import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

^///?}

public class ItemRendering {

	public static boolean SWAP_TARGET = false;

	private static final int WIDTH = 16;
	private static final int HEIGHT = 16;

	@Nullable
	public static volatile TextureTarget TARGET;

	@SuppressWarnings("deprecation")
	public static void renderFluidIntoImage(BucketItem bucketItem, Consumer<RenderedFluidImage> consumer) {
		FamilySafeRenderExecutor.submit(() -> {
			//? if fabric || neoforge {
			Fluid content = bucketItem.content;
			//?} elif forge {
			/^Fluid content = bucketItem.getFluid();
			 ^///?}

			FluidState fluidState = content.defaultFluidState();
			//? if fabric {
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(content);
			if (handler == null) {
				consumer.accept(null);
				return;
			}

			TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, fluidState);

			TextureAtlasSprite sprite = fluidSprites[0];

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().originalImage.copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return handler.getFluidColor(null, null, fluidState);
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return handler.getFluidColor(level, pos, fluidState);
				}
			}));
			//?} else {
			/^IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluidState);
			TextureAtlas atlas;
			try {
				atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
			} catch (Exception e) {
				consumer.accept(null);
				throw new RuntimeException(e);
			}

			Identifier stillTexture = extensions.getStillTexture();
			if (stillTexture == null) { // can be null!! Ignore warning
				consumer.accept(null);
				return;
			}

			//? if neoforge {
			/^¹TextureAtlasSprite sprite = atlas.getTextures().get(stillTexture);
			¹^///?} else {
			TextureAtlasSprite sprite = atlas.getSprite(stillTexture);
			//?}
			if (sprite == null) { // can be null!! Ignore warning
				consumer.accept(null);
				return;
			}

			NativeImage nativeImage = new NativeImage(WIDTH, HEIGHT, true);
			sprite.contents().getOriginalImage().copyRect(nativeImage, 0, 0, 0, 0, 16, 16, false, false);

			consumer.accept(new RenderedFluidImage(nativeImage, new ColorGetter() {
				@Override
				public int getFallback(BlockState state) {
					return extensions.getTintColor();
				}

				@Override
				public int getWorld(BlockState state, ClientLevel level, BlockPos pos) {
					return extensions.getTintColor(fluidState, level, pos);
				}
			}));
			^///?}
		});
	}

	public static void renderItemIntoImage(ItemStack itemStack, Consumer<RenderedItemImage> consumer) {
		RenderData renderData = ItemRendering.checkAndGetTarget();
		FamilySafeRenderExecutor.submit(() -> {
			ItemRendering.renderItemStackToTarget(renderData, itemStack);

			TextureTarget target = renderData.target;
			NativeImage nativeImage = new NativeImage(target.width, target.height, false);
			RenderSystem.bindTexture(target.getColorTextureId());
			nativeImage.downloadTexture(0, false);
			nativeImage.flipY();

			consumer.accept(new RenderedItemImage(nativeImage));
		});
	}

	private static void renderItemStackToTarget(RenderData data, ItemStack itemStack) {
		TextureTarget target = data.target;

		//? if >=1.21.1 {
		var oldModelViewMatrix = RenderSystem.getModelViewStack();
		//?} else {
		/^PoseStack oldMatrix = new PoseStack();
		copyFrom(oldMatrix, RenderSystem.getModelViewStack());
		^///?}
		RenderSystem.backupProjectionMatrix();

		target.bindWrite(true);
		GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 0.0F);
		int a = 16384;
		GlStateManager._clearDepth(1.0);
		a |= 256;
		GlStateManager._clear(a, Minecraft.ON_OSX);

		Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, WIDTH / 2F, HEIGHT / 2F, 0.0F, -5000F, 5000F);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		//? if >=1.21.1 {
		RenderSystem.getModelViewStack().identity();
		//?} else {
		/^RenderSystem.getModelViewStack().setIdentity();
		 ^///?}

		ItemRendering.renderItemStack(itemStack, target);

		//? if >=1.21.1 {
		RenderSystem.getModelViewStack().set(oldModelViewMatrix);
		 //?} else {
		/^copyFrom(RenderSystem.getModelViewStack(), oldMatrix);
		^///?}
		RenderSystem.restoreProjectionMatrix();
	}

	//? if <=1.20.1 {
	/^public static void copyFrom(PoseStack entry, PoseStack anotherEntry) {
		entry.last().pose().set(anotherEntry.last().pose());
		entry.last().normal().set(anotherEntry.last().normal());
	}
	^///?}

	private static void renderItemStack(ItemStack itemStack, TextureTarget target) {
		BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);

		PoseStack poseStack = new PoseStack();
		poseStack.pushPose();
		poseStack.last().pose().mul(new Matrix4f());
		float max = Math.max(WIDTH, HEIGHT) / 2F;

		poseStack.translate(max / 2F, max / 2F, max / 2F);
		poseStack.scale(max, -max, max);

		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

		bufferSource.endBatch();
		Lighting.setupForFlatItems();
		SWAP_TARGET = true;
		target.bindWrite(true);

		Minecraft.getInstance().getItemRenderer().render(
				itemStack,
				ItemDisplayContext.GUI,
				false,
				poseStack,
				bufferSource,
				15728880,
				OverlayTexture.NO_OVERLAY,
				model
		);

		bufferSource.endBatch();
		Lighting.setupFor3DItems();
		SWAP_TARGET = false;

		Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
	}

	@NotNull
	private static RenderData checkAndGetTarget() {
		if (TARGET == null) {
			FamilySafeRenderExecutor.submit(() -> {
				TARGET = new TextureTarget(WIDTH, HEIGHT, true, Minecraft.ON_OSX);
			});
		}
		TextureTarget target;

		do {
			target = TARGET;
		} while (target == null);

		return new RenderData(target);
	}

	private record RenderData(TextureTarget target) {}
}
*///?}