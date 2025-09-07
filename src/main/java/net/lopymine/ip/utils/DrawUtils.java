package net.lopymine.ip.utils;

//? if <=1.21.1 {
/*import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.jetbrains.annotations.Nullable;
*///?}

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import org.joml.Matrix4f;

public class DrawUtils {

	public static void drawParticleSprite(
			DrawContext context,
			Sprite sprite,
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
		context.drawSpriteStretched(
				/*? if >=1.21.6 {*/ net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, /*?} elif >=1.21.2 {*/ /*net.minecraft.client.render.RenderLayer::getGuiTextured,*//*?}*/
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
		float u1 = sprite.getMinU();
		float u2 = sprite.getMaxU();
		float v1 = sprite.getMinV();
		float v2 = sprite.getMaxV();

		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		PARTICLES_BUFFER.vertex(matrix4f, (float) x, (float) y, z).texture(u1, v1).color(color)/^? if <=1.20.1 {^//^.next()^//^?}^/;
		PARTICLES_BUFFER.vertex(matrix4f, (float) x, y2, z).texture(u1, v2).color(color)/^? if <=1.20.1 {^//^.next()^//^?}^/;
		PARTICLES_BUFFER.vertex(matrix4f, x2, y2, z).texture(u2, v2).color(color)/^? if <=1.20.1 {^//^.next()^//^?}^/;
		PARTICLES_BUFFER.vertex(matrix4f, x2, (float) y, z).texture(u2, v1).color(color)/^? if <=1.20.1 {^//^.next()^//^?}^/;
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
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, InventoryParticlesAtlasManager.ATLAS_ID);
		com.mojang.blaze3d.systems.RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		//? if >=1.21 {
		PARTICLES_BUFFER = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		//?} else {
		/^BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		PARTICLES_BUFFER = buffer;
		^///?}
	}

	public static void endParticlesBuffer() {
		if (PARTICLES_BUFFER == null) {
			return;
		}
		BufferRenderer.drawWithGlobalProgram(PARTICLES_BUFFER.end());
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();
		com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		PARTICLES_BUFFER = null;
	}
	*///?} else {
	public static void prepareParticlesBuffer() {}
	public static void endParticlesBuffer() {}
	//?}
}
