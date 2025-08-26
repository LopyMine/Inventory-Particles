package net.lopymine.ip.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

@SuppressWarnings("unused")
public class DrawUtils {

	public static void drawTexture(DrawContext context, Identifier sprite, int x, int y, float u, float v, float width, float height, float textureWidth, float textureHeight, int color) {
		drawTexture(
				context,
				/*? if >=1.21.6 {*/ net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, /*?} elif >=1.21.2 {*/ /*net.minecraft.client.render.RenderLayer::getGuiTextured,*//*?}*/
				sprite,
				x,
				y,
				u,
				v,
				width,
				height,
				width,
				height,
				textureWidth,
				textureHeight,
				color
		);
	}

	public static void drawTexture(
			DrawContext context,
			/*? if >=1.21.6 {*/ com.mojang.blaze3d.pipeline.RenderPipeline o, /*?} elif >=1.21.2 {*/ /*Function<Identifier, RenderLayer> o,*//*?}*/
			Identifier sprite,
			int x,
			int y,
			float u,
			float v,
			float width,
			float height,
			float regionWidth,
			float regionHeight,
			float textureWidth,
			float textureHeight,
			int color
	) {
		/*? if >=1.21.8 {*/context./*?}*/drawTexturedQuad(
				/*? if <=1.21.7 {*//*context,*//*?}*/
				o,
				sprite,
				x,
				(int) (x + width),
				y,
				(int) (y + height),
				(u + 0.0F) / textureWidth,
				(u + regionWidth) / textureWidth,
				(v + 0.0F) / textureHeight,
				(v + regionHeight) / textureHeight,
				color
		);
	}

	//? if <=1.21.7 {
	/*private static void drawTexturedQuad(
			DrawContext context,
			Function<Identifier, RenderLayer> renderLayers,
			Identifier sprite,
			int x1,
			int x2,
			int y1,
			int y2,
			float u1,
			float u2,
			float v1,
			float v2,
			int color
	) {
		RenderLayer renderLayer = renderLayers.apply(sprite);
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		VertexConsumer vertexConsumer = context.vertexConsumers.getBuffer(renderLayer);
		vertexConsumer.vertex(matrix4f, x1, y1, 0.0F).texture(u1, v1).color(color);
		vertexConsumer.vertex(matrix4f, x1, y2, 0.0F).texture(u1, v2).color(color);
		vertexConsumer.vertex(matrix4f, x2, y2, 0.0F).texture(u2, v2).color(color);
		vertexConsumer.vertex(matrix4f, x2, y1, 0.0F).texture(u2, v1).color(color);
	}
	*///?}
}
