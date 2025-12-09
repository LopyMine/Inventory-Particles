package net.lopymine.ip.utils;

//? if <=1.21.1 {
/*import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
*///?}

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class ParticleDrawUtils {

	public static void drawParticleSprite(
			GuiGraphics context,
			TextureAtlasSprite sprite,
			int x,
			int y,
			float width,
			float height,
			int color
	) {
		//? if >=1.21.2 && <=1.21.4 {
		/*//com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		*///?}

		//? if >=1.21.2 {
		context.blitSprite(
				/*? if >=1.21.6 {*/ net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, /*?} elif >=1.21.2 {*/ /*net.minecraft.client.renderer.RenderType::guiTextured,*//*?}*/
				sprite,
				x,
				y,
				(int) width,
				(int) height,
				color
		);
		//?} else {
		/*if (PARTICLES_BUFFER == null) {
			return;
		}

		float x2 = x + width;
		float y2 = y + height;
		float z = 0F;
		float u1 = sprite.getU0();
		float u2 = sprite.getU1();
		float v1 = sprite.getV0();
		float v2 = sprite.getV1();

		Matrix4f matrix4f = context.pose().last().pose();

		//? if >=1.21 {
		PARTICLES_BUFFER.addVertex(matrix4f, (float) x, (float) y, z).setUv(u1, v1).setColor(color);
		PARTICLES_BUFFER.addVertex(matrix4f, (float) x, y2, z).setUv(u1, v2).setColor(color);
		PARTICLES_BUFFER.addVertex(matrix4f, x2, y2, z).setUv(u2, v2).setColor(color);
		PARTICLES_BUFFER.addVertex(matrix4f, x2, (float) y, z).setUv(u2, v1).setColor(color);
		//?} else {
		/^PARTICLES_BUFFER.vertex(matrix4f, (float) x, (float) y, z).uv(u1, v1).color(color).endVertex();
		PARTICLES_BUFFER.vertex(matrix4f, (float) x, y2, z).uv(u1, v2).color(color).endVertex();
		PARTICLES_BUFFER.vertex(matrix4f, x2, y2, z).uv(u2, v2).color(color).endVertex();
		PARTICLES_BUFFER.vertex(matrix4f, x2, (float) y, z).uv(u2, v1).color(color).endVertex();
		^///?}

		*///?}

		//? if >=1.21.2 && <=1.21.4 {
		/*//com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();
		*///?}
	}

	//? if <=1.21.1 {
	/*@Nullable
	private static BufferBuilder PARTICLES_BUFFER = null;

	public static void prepareParticlesBuffer() {
		if (PARTICLES_BUFFER != null) {
			endParticlesBuffer();
		}
		com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, InventoryParticlesAtlasManager.ATLAS_ID);
		com.mojang.blaze3d.systems.RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		//? if >=1.21 {
		PARTICLES_BUFFER = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		//?} else {
		/^BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		PARTICLES_BUFFER = buffer;
		^///?}
	}

	public static void endParticlesBuffer() {
		if (PARTICLES_BUFFER == null) {
			return;
		}
		//? if >=1.21 {
		BufferUploader.drawWithShader(PARTICLES_BUFFER.buildOrThrow());
		//?} else {
		/^BufferUploader.drawWithShader(PARTICLES_BUFFER.end());
		^///?}
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
		PARTICLES_BUFFER = null;
	}
	*///?} else {
	public static void prepareParticlesBuffer() {}
	public static void endParticlesBuffer() {}
	//?}
}
